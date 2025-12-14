package com.bigdatacompany.eticaret.repository;

import com.bigdatacompany.eticaret.model.TimeStat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Zaman bazlı istatistikler için MongoDB repository
 */
@Repository
public interface TimeStatRepository extends MongoRepository<TimeStat, String> {

    /**
     * Belirli bir tarihteki tüm saatlik istatistikleri getir
     */
    List<TimeStat> findByDateOrderByHourAsc(String date);

    /**
     * Tüm verileri tarihe göre sıralı getir
     */
    List<TimeStat> findAllByOrderByDateDescHourDesc();
}

