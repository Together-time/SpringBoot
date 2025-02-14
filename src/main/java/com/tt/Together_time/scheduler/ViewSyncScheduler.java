package com.tt.Together_time.scheduler;

import com.tt.Together_time.domain.mongodb.ProjectDocument;
import com.tt.Together_time.repository.ProjectMongoRepository;
import com.tt.Together_time.repository.RedisDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewSyncScheduler {
    private final RedisDao redisDao;
    private final ProjectMongoRepository projectMongoRepository;

    @Scheduled(fixedRate = 600000) // 10분
    public void syncViewsToMongoDB() {
        log.info("조회수 동기화 시작");

        Set<String> keys = redisDao.getKeysByPattern("views:*");
        if (keys.isEmpty()) {
            log.info("동기화할 조회수가 없음.");
            return;
        }

        for (String key : keys) {
            Long projectId = Long.parseLong(key.replace("views:", ""));
            Long redisViews = Long.parseLong(redisDao.getValues(key));

            // MongoDB에서 현재 조회수 가져오기
            ProjectDocument projectDocument = projectMongoRepository.findByProjectId(projectId).get();
            if (projectDocument != null) {
                Long mongoViews = projectDocument.getViews();

                if (mongoViews > redisViews) {
                    redisDao.setValuesWithTTL(key, String.valueOf(mongoViews), 3600); // 다시 Redis 초기화
                } else {
                    projectDocument.setViews(redisViews);
                    projectMongoRepository.save(projectDocument);
                }
            }
        }
    }
}
