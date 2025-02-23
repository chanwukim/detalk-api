package net.detalk.api.plan.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class PricingPlanNotFoundException extends ApiException {

    public PricingPlanNotFoundException(String name) {
        super(String.format("가격 정책(NAME: %s)을 찾을 수 없습니다.", name));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "pricing_plan_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
