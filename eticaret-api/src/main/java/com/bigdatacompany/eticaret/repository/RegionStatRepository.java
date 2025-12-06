package com.bigdatacompany.eticaret.repository;

import com.bigdatacompany.eticaret.model.RegionStat;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Bölge istatistikleri için MongoDB repository
 */
@Repository
public interface RegionStatRepository extends MongoRepository<RegionStat, String> {

    /**
     * Son batch'teki bölge istatistiklerini getir
     */
    List<RegionStat> findTop10ByOrderByBatchIdDescCountDesc();

    /**
     * En son batch ID'sini bul ve o batch'teki tüm kayıtları getir
     */
    @Aggregation(pipeline = {
            "{ $sort: { batchId: -1 } }",
            "{ $limit: 1 }",
            "{ $lookup: { from: 'region_stats', localField: 'batchId', foreignField: 'batchId', as: 'stats' } }",
            "{ $unwind: '$stats' }",
            "{ $replaceRoot: { newRoot: '$stats' } }",
            "{ $sort: { count: -1 } }",
            "{ $limit: 10 }"
    })
    List<RegionStat> findLatestBatchStats();
}
