package com.donttaz.donttazapi.company.controller;

import com.donttaz.donttazapi.company.dto.request.CreateCompanyRequest;
import com.donttaz.donttazapi.company.dto.request.PaySalaryRequest;
import com.donttaz.donttazapi.company.dto.request.RegisterCompanyAccountRequest;
import com.donttaz.donttazapi.company.dto.response.CompanyAccountResponse;
import com.donttaz.donttazapi.company.dto.response.CompanyResponse;
import com.donttaz.donttazapi.company.service.CompanySalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Company Salary API", description = "기업 급여 지급 및 관리용 API (Swagger 전용)")
@RestController
@RequestMapping("/api/v1/company/salary")
@RequiredArgsConstructor
public class CompanySalaryController {

    private final CompanySalaryService companySalaryService;

    /**
     * 신규 기업 정보를 시스템에 등록함.
     */
    @Operation(summary = "기업 등록", description = "새로운 기업 정보를 시스템에 등록함.")
    @PostMapping("/company")
    public Long createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        return companySalaryService.createCompany(request.name());
    }

    /**
     * 기업의 실제 금융 계좌를 연동함.
     */
    @Operation(summary = "기업 계좌 등록", description = "기업 아이디에 실제 금융 계좌 연동함.")
    @PostMapping("/account")
    public void registerCompanyAccount(@Valid @RequestBody RegisterCompanyAccountRequest request) {
        companySalaryService.registerCompanyAccount(request.companyId(), request.userId(), request.accountNumber());
    }

    /**
     * 등록된 전체 기업 목록 조회함.
     */
    @Operation(summary = "기업 목록 조회", description = "시스템에 등록된 전체 기업 리스트 반환함.")
    @GetMapping("/companies")
    public List<CompanyResponse> getCompanies() {
        return companySalaryService.getCompanies();
    }

    /**
     * 특정 기업의 계좌 목록 조회함.
     */
    @Operation(summary = "기업 계좌 조회", description = "특정 기업 아이디에 연동된 계좌 리스트 반환함.")
    @GetMapping("/companies/{companyId}/accounts")
    public List<CompanyAccountResponse> getCompanyAccounts(@PathVariable("companyId") Long companyId) {
        return companySalaryService.getCompanyAccounts(companyId);
    }

    /**
     * 급여 이체를 실행함.
     * 적요는 서비스 레이어에서 자동 생성함.
     */
    @Operation(summary = "급여 지급", description = "기업 계좌에서 사용자 계좌로 월급 이체함.")
    @PostMapping("/pay")
    public void paySalary(@Valid @RequestBody PaySalaryRequest request) {
        companySalaryService.paySalary(request.companyAccountId(), request.userAccountNo(), request.amount());
    }
}
