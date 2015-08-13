package com.techlooper.controller;

import com.techlooper.entity.JobAlertRegistrationEntity;
import com.techlooper.entity.ScrapeJobEntity;
import com.techlooper.model.JobAlertRegistration;
import com.techlooper.service.JobAlertService;
import com.techlooper.util.DateTimeUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Controller
public class JobAlertController {

    @Value("${jobAlert.period}")
    private int CONFIGURED_JOB_ALERT_PERIOD;

    @Resource
    private JobAlertService jobAlertService;

    @ResponseBody
    @RequestMapping(value = "jobAlert/register", method = RequestMethod.POST)
    public JobAlertRegistrationEntity getCompany(@RequestBody JobAlertRegistration jobAlertRegistration) throws Exception {
        JobAlertRegistrationEntity jobAlertRegistrationEntity = jobAlertService.registerJobAlert(jobAlertRegistration);
        List<ScrapeJobEntity> scrapeJobEntities = jobAlertService.searchJob(jobAlertRegistrationEntity);

        if (!scrapeJobEntities.isEmpty()) {
            jobAlertService.sendEmail(jobAlertRegistrationEntity, scrapeJobEntities);
        }

        return jobAlertRegistrationEntity;
    }

    @Scheduled(cron = "${scheduled.cron.jobAlert}")
    public void sendJobAlertEmail() throws Exception {
        List<JobAlertRegistrationEntity> jobAlertRegistrationEntities =
                jobAlertService.searchJobAlertRegistration(CONFIGURED_JOB_ALERT_PERIOD);

        if (!jobAlertRegistrationEntities.isEmpty()) {
            for (JobAlertRegistrationEntity jobAlertRegistrationEntity : jobAlertRegistrationEntities) {
                boolean isAlreadySentToday = checkIfEmailAlreadySentToday(jobAlertRegistrationEntity.getLastEmailSentDateTime());

                if (!isAlreadySentToday) {
                    List<ScrapeJobEntity> scrapeJobEntities = jobAlertService.searchJob(jobAlertRegistrationEntity);
                    if (!scrapeJobEntities.isEmpty()) {
                        jobAlertService.sendEmail(jobAlertRegistrationEntity, scrapeJobEntities);
                    }
                }
            }
        }
    }

    private boolean checkIfEmailAlreadySentToday(String lastEmailSentDateTime) {
        if (lastEmailSentDateTime == null) {
            return false;
        }

        try {
            Date lastSentDate = DateTimeUtils.parseString2Date(lastEmailSentDateTime, "dd/MM/yyyy HH:mm");
            Date currentDate = new Date();
            int diffDays = Days.daysBetween(new DateTime(lastSentDate), new DateTime(currentDate)).getDays();
            return diffDays == 0 ? true : false;
        } catch (ParseException e) {
            return false;
        }
    }

}
