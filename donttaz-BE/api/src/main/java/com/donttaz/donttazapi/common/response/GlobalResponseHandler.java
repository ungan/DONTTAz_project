package com.donttaz.donttazapi.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.donttaz.donttazapi")
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 이미 ApiResponse 형태로 감싸진 경우(예외 처리기 등) 중복 포장 방지
        return !returnType.getParameterType().isAssignableFrom(ApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {

        // Swagger 등의 문서화 요청이나 문자열 반환 시 처리 예외가 필요할 수 있음
        // 문자열을 반환하는 컨트롤러가 있다면 StringHttpMessageConverter 에러를 피하기 위해 ObjectMapper로 직렬화
        // 필요
        // 현재 API 구조는 객체 위주이므로 기본적으로 ApiResponse.success 로 포장

        String requestPath = request.getURI().getPath();
        if (requestPath.contains("/v3/api-docs") || requestPath.contains("/swagger-ui")) {
            return body;
        }

        if (body instanceof ApiResponse<?>) {
            return body;
        }

        return ApiResponse.success("요청이 성공적으로 처리되었습니다.", body);
    }
}
