package cn.maiaimei.config;

import cn.maiaimei.annotation.SkipWrapResponse;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class SkipWrapResponseCustomizer implements OperationCustomizer {

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    if (handlerMethod.hasMethodAnnotation(SkipWrapResponse.class)) {
      operation.addExtension("x-skip-wrap-response", true);
    }
    return operation;
  }
}