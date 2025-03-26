package com.example.demo.repository;

import com.example.demo.entity.Bank;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    @Query("SELECT b FROM Bank b")
    Stream<Bank> findBankA(PageRequest pageRequest);

    default Stream<Bank> findBankLimited() {
        PageRequest pageRequest = PageRequest.of(0, 100000); // 0 is the page number, 5000000 is the page size
        return findBankA(pageRequest);
    }

//    @Query("SELECT b FROM Bank b")
//    List<Bank> findBankA(PageRequest pageRequest);
//
//    default List<Bank> findBankLimited() {
//        PageRequest pageRequest = PageRequest.of(0, 100000); // 0 is the page number, 5000000 is the page size
//        return findBankA(pageRequest);
//    }

//    @Query("SELECT b FROM Bank b WHERE b.code='BANKA' LIMIT 5000000")
//    Stream<Bank> findBankA();
}
