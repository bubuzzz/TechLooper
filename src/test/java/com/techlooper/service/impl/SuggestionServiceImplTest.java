package com.techlooper.service.impl;

import com.techlooper.config.ElasticsearchConfiguration;
import com.techlooper.config.SuggestionServiceConfigurationTest;
import com.techlooper.service.SuggestionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchConfiguration.class, SuggestionServiceConfigurationTest.class})
public class SuggestionServiceImplTest {

    @Resource
    private SuggestionService suggestionService;

    @Test
    public void testSuggestSkills() throws Exception {
        String query = "Java";
        List<String> skills = suggestionService.suggestSkills(query);
        skills.forEach(skill -> assertTrue(skill.toLowerCase().contains(query.toLowerCase())));
    }

    @Test
    public void testSuggestSkillsNoSkill() throws Exception {
        String query = "ABC XYZ";
        List<String> skills = suggestionService.suggestSkills(query);
        assertTrue(skills.isEmpty());
    }
}