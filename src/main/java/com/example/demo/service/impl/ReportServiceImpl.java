package com.example.demo.service.impl;

import com.example.demo.entity.Bank;
import com.example.demo.helper.ExcelHelper;
import com.example.demo.repository.BankRepository;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ExcelHelper excelHelper;
    private final BankRepository bankRepository;

    @Override
    public byte[] downloadsFiles() {

        long startTime = System.currentTimeMillis();
        log.info("Exporting excel started.");

        try (Stream<Bank> bankStream = bankRepository.findBankLimited()) {

            log.info("Downloading bank data...");
            String header = "Report Generated " + new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a").format(new Date());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            excelHelper.exportToExcel(bankStream, header, outputStream);
            byte[] byteData = outputStream.toByteArray();
            log.info("Exporting excel completed. Time taken: {} ms", (System.currentTimeMillis() - startTime));

            return byteData;
        } catch (Exception e) {
            log.error("Error occurred while downloading file", e);
            throw new RuntimeException("Error occurred while downloading file", e);
        }
    }

}
