package com.bigdatacompany.eticaret.repository;

import com.bigdatacompany.eticaret.model.SearchStat;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Arama istatistikleri için MongoDB repository
 */
@Repository
public interface SearchStatRepository extends MongoRepository<SearchStat, String> {

    /**
     * Son batch'teki arama istatistiklerini getir
     */
    List<SearchStat> findTop10ByOrderByBatchIdDescCountDesc();

    /**
     * En son batch ID'sini bul ve o batch'teki tüm kayıtları getir
     */
    @Aggregation(pipeline = {
            "{ $sort: { batchId: -1 } }",
            "{ $limit: 1 }",
            "{ $lookup: { from: 'search_stats', localField: 'batchId', foreignField: 'batchId', as: 'stats' } }",
            "{ $unwind: '$stats' }",
            "{ $replaceRoot: { newRoot: '$stats' } }",
            "{ $sort: { count: -1 } }",
            "{ $limit: 10 }"
    })
    List<SearchStat> findLatestBatchStats();

    /**
     * Belirli bir arama teriminin toplam sayısını getir
     */
    @Aggregation(pipeline = {
            "{ $match: { search: ?0 } }",
            "{ $group: { _id: '$search', totalCount: { $sum: '$count' } } }"
    })
    Long getTotalCountBySearch(String search);
}
