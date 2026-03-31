package com.donttaz.donttazapi.company.controller;

import com.donttaz.donttazapi.company.dto.request.CreateCompanyRequest;
import com.donttaz.donttazapi.company.dto.request.PaySalaryRequest;
import com.donttaz.donttazapi.company.dto.response.CompanyAccountResponse;
import com.donttaz.donttazapi.company.dto.response.CompanyResponse;
import com.donttaz.donttazapi.company.dto.response.UserAccountSummaryResponse;
import com.donttaz.donttazapi.company.service.CompanySalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
@SecurityRequirements({}) // Swagger 글로벌 인증 요구 사항 제거함
@RestController
@RequestMapping("/api/v1/company/salary")
@RequiredArgsConstructor
public class CompanySalaryController {

    private final CompanySalaryService companySalaryService;

    /**
     * 기업 정보 생성 (계좌 개설 및 자금 1억 충전 자동화)
     */
    @Operation(summary = "기업 등록", description = "새로운 기업 정보를 등록하고, 계좌 개설 및 1억 원 입금까지 한 번에 자동 처리함.")
    @PostMapping("/company")
    public Long createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        return companySalaryService.createCompany(request.name(), request.userId());
    }

    /**
     * 기업 계좌 생성
     * (기업 등업 API 3단계 통합으로 인해 주석 처리됨.)
     */
    // @Operation(summary = "기업 계좌 생성", description = "금융 API를 통해 기업 명의의 계좌를 직접 생성 및 등록함.")
    // @PostMapping("/account")
    // public void createCompanyAccount(@Valid @RequestBody RegisterCompanyAccountRequest request) {
    //     companySalaryService.createCompanyAccount(request.companyId(), request.userId());
    // }

    /**
     * 전체 기업 목록 조회
     */
    @Operation(summary = "기업 목록 조회", description = "시스템에 등록된 전체 기업 리스트 반환함.")
    @GetMapping("/companies")
    public List<CompanyResponse> getCompanies() {
        return companySalaryService.getCompanies();
    }

    /**
     * 특정 기업 계좌 목록 조회
     */
    @Operation(summary = "기업 계좌 조회", description = "특정 기업 아이디에 연동된 계좌 리스트 반환함.")
    @GetMapping("/companies/{companyId}/accounts")
    public List<CompanyAccountResponse> getCompanyAccounts(@PathVariable("companyId") Long companyId) {
        return companySalaryService.getCompanyAccounts(companyId);
    }

    /**
     * 급여 이체 실행
     */
    @Operation(summary = "급여 지급", description = "기업 계좌에서 사용자 계좌로 월급 이체함.")
    @PostMapping("/pay")
    public void paySalary(@Valid @RequestBody PaySalaryRequest request) {
        companySalaryService.paySalary(request.companyAccountId(), request.userAccountNo(), request.amount());
    }

    /**
     * 특정 기업 계좌 1억 원 입금 (테스트용)
     */
    @Operation(summary = "기업 자금 1억 입금", description = "기업 ID를 기반으로 해당 기업의 첫 번째 계좌에 1억 원을 강제로 입금함. (잔액 부족 방지용)")
    @PostMapping("/companies/{companyId}/deposit")
    public void depositCompanyFund(@PathVariable("companyId") Long companyId) {
        companySalaryService.deposit100Million(companyId);
    }

    /**
     * 전체 사용자 계좌 리스트 조회 (테스트용)
     */
    @Operation(summary = "전체 사용자 계좌 목록 조회", description = "급여 이체 테스트 시 대상 계좌를 찾기 쉽도록, 전체 활성 사용자의 이름과 계좌 번호를 조회함.")
    @GetMapping("/users/accounts")
    public List<UserAccountSummaryResponse> getAllUserAccounts() {
        return companySalaryService.getAllUserAccounts();
    }
}
