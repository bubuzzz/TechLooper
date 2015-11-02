package com.techlooper.service.impl;

import com.techlooper.dto.WinnerDto;
import com.techlooper.entity.ChallengeEntity;
import com.techlooper.entity.ChallengeRegistrantDto;
import com.techlooper.entity.ChallengeRegistrantEntity;
import com.techlooper.entity.ChallengeSubmissionEntity;
import com.techlooper.model.ChallengePhaseEnum;
import com.techlooper.model.ChallengeRegistrantPhaseItem;
import com.techlooper.model.ChallengeSubmissionDto;
import com.techlooper.model.RewardEnum;
import com.techlooper.repository.elasticsearch.ChallengeRegistrantRepository;
import com.techlooper.repository.elasticsearch.ChallengeRepository;
import com.techlooper.repository.elasticsearch.ChallengeSubmissionRepository;
import com.techlooper.service.ChallengeRegistrantService;
import com.techlooper.service.ChallengeService;
import org.dozer.Mapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.techlooper.model.ChallengePhaseEnum.*;
import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ChallengeRegistrantServiceImpl implements ChallengeRegistrantService {

  @Resource
  private ElasticsearchTemplate elasticsearchTemplate;

  private final List<ChallengePhaseEnum> CHALLENGE_PHASES = Arrays.asList(FINAL, PROTOTYPE, UIUX, IDEA);

  @Resource
  private ChallengeService challengeService;

  @Resource
  private ChallengeRegistrantRepository challengeRegistrantRepository;

  @Resource
  private Mapper dozerMapper;

  @Resource
  private ChallengeSubmissionRepository challengeSubmissionRepository;

  @Resource
  private ChallengeRepository challengeRepository;

  public Map<ChallengePhaseEnum, ChallengeRegistrantPhaseItem> countNumberOfRegistrantsByPhase(Long challengeId) {
    Map<ChallengePhaseEnum, ChallengeRegistrantPhaseItem> numberOfRegistrantsByPhase = new HashMap<>();

    NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder().withIndices("techlooper")
      .withTypes("challengeRegistrant").withSearchType(SearchType.COUNT);
    searchQueryBuilder.withQuery(termQuery("challengeId", challengeId));

    Long numberOfRegistrants = elasticsearchTemplate.count(searchQueryBuilder.build());
    numberOfRegistrantsByPhase.put(REGISTRATION, new ChallengeRegistrantPhaseItem(REGISTRATION, numberOfRegistrants));

    searchQueryBuilder.addAggregation(AggregationBuilders.terms("sumOfRegistrants").field("activePhase"));
    Aggregations aggregations = elasticsearchTemplate.query(searchQueryBuilder.build(), SearchResponse::getAggregations);
    Terms terms = aggregations.get("sumOfRegistrants");


    Long previousPhase = 0L;
    for (ChallengePhaseEnum phaseEnum : CHALLENGE_PHASES) {
      Terms.Bucket bucket = terms.getBucketByKey(phaseEnum.getValue());
      if (bucket != null) {
        numberOfRegistrantsByPhase.put(phaseEnum, new ChallengeRegistrantPhaseItem(phaseEnum,
          bucket.getDocCount() + previousPhase));
        previousPhase = bucket.getDocCount() + previousPhase;
      }
      else {
        bucket = terms.getBucketByKey(phaseEnum.getValue().toLowerCase());
        if (bucket != null) {
          numberOfRegistrantsByPhase.put(phaseEnum, new ChallengeRegistrantPhaseItem(phaseEnum,
            bucket.getDocCount() + previousPhase));
          previousPhase = bucket.getDocCount() + previousPhase;
        }
        else {
          numberOfRegistrantsByPhase.put(phaseEnum, new ChallengeRegistrantPhaseItem(phaseEnum, previousPhase));
        }
      }
    }
    return numberOfRegistrantsByPhase;
  }

  public Long countNumberOfWinners(Long challengeId) {
    NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder().withIndices("techlooper")
      .withTypes("challengeRegistrant").withSearchType(SearchType.COUNT);

    BoolFilterBuilder boolFilterBuilder = boolFilter();
    boolFilterBuilder.must(termFilter("challengeId", challengeId));
    boolFilterBuilder.must(termFilter("activePhase", ChallengePhaseEnum.FINAL.getValue()));
    boolFilterBuilder.mustNot(missingFilter("criteria"));

    searchQueryBuilder.withQuery(filteredQuery(matchAllQuery(), boolFilterBuilder));
    return elasticsearchTemplate.count(searchQueryBuilder.build());
  }

  public Set<ChallengeRegistrantDto> findRegistrantsByChallengeIdAndPhase(Long challengeId, ChallengePhaseEnum phase, String ownerEmail) {
    if (!challengeService.isOwnerOfChallenge(ownerEmail, challengeId)) {
      return null;
    }

    if (phase == WINNER) {
      return findWinnerRegistrantsByChallengeId(challengeId);
    }

    BoolQueryBuilder challengeQuery = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("challengeId", challengeId));
    BoolQueryBuilder activePhaseQuery = QueryBuilders.boolQuery();
    BoolQueryBuilder submissionPhaseQuery = QueryBuilders.boolQuery();
    challengeQuery.must(activePhaseQuery);

    if (phase == REGISTRATION) {
      activePhaseQuery.should(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.missingFilter("activePhase")));
      submissionPhaseQuery.should(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.missingFilter("submissionPhase")));
    }
    for (int i = ChallengePhaseEnum.ALL_CHALLENGE_PHASES.length - 1; i >= 0; i--) {
      activePhaseQuery.should(QueryBuilders.termQuery("activePhase", ALL_CHALLENGE_PHASES[i]));
      submissionPhaseQuery.should(QueryBuilders.termQuery("submissionPhase", ALL_CHALLENGE_PHASES[i]));
      if (phase == ALL_CHALLENGE_PHASES[i]) break;
    }

    Set<ChallengeRegistrantDto> registrants = StreamUtils.createStreamFromIterator(challengeRegistrantRepository.search(challengeQuery).iterator())
      .map(registrant -> {
        ChallengeRegistrantDto dto = dozerMapper.map(registrant, ChallengeRegistrantDto.class);
        BoolQueryBuilder submissionQuery = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("registrantId", registrant.getRegistrantId()))
          .must(submissionPhaseQuery);
        List<ChallengeSubmissionDto> submissions = StreamUtils.createStreamFromIterator(challengeSubmissionRepository.search(submissionQuery).iterator())
          .map(submission -> dozerMapper.map(submission, ChallengeSubmissionDto.class)).collect(Collectors.toList());
        dto.setSubmissions(submissions);
        return dto;
      }).collect(Collectors.toSet());

    return registrants;
  }

  public Set<ChallengeRegistrantDto> findWinnerRegistrantsByChallengeId(Long challengeId) {

    BoolQueryBuilder winnerQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.termQuery("challengeId", challengeId))
      .must(QueryBuilders.termQuery("activePhase", ChallengePhaseEnum.FINAL))
      .must(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.existsFilter("criteria.score")));

    Set<ChallengeRegistrantDto> registrants = StreamUtils.createStreamFromIterator(challengeRegistrantRepository.search(winnerQuery).iterator())
      .map(registrant -> {
        ChallengeRegistrantDto dto = dozerMapper.map(registrant, ChallengeRegistrantDto.class);
        BoolQueryBuilder submissionQuery = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("registrantId", registrant.getRegistrantId()))
          .must(QueryBuilders.termQuery("submissionPhase", ChallengePhaseEnum.FINAL));
        List<ChallengeSubmissionDto> submissions = StreamUtils.createStreamFromIterator(challengeSubmissionRepository.search(submissionQuery).iterator())
          .map(submission -> dozerMapper.map(submission, ChallengeSubmissionDto.class)).collect(Collectors.toList());
        dto.setSubmissions(submissions);
        return dto;
      }).collect(Collectors.toSet());
    return registrants;
  }

  public boolean saveWinner(WinnerDto winnerDto, String loginUser) {
    Long registrantId = winnerDto.getRegistrantId();
    RewardEnum reward = winnerDto.getReward();
    ChallengeRegistrantEntity registrant = challengeRegistrantRepository.findOne(registrantId);
    if (!challengeService.isOwnerOfChallenge(loginUser, registrant.getChallengeId())) {
      return false;
    }

    ChallengeEntity challenge = challengeRepository.findOne(registrant.getChallengeId());
    switch (reward) {
      case FIRST_PLACE:
        challenge.setFirstPlaceRewardWinnerId(registrantId);
        break;

      case SECOND_PLACE:
        challenge.setSecondPlaceRewardWinnerId(registrantId);
        break;

      case THIRD_PLACE:
        challenge.setThirdPlaceRewardWinnerId(registrantId);
        break;
    }

    challenge = challengeRepository.save(challenge);
    return challenge != null;
  }

}
