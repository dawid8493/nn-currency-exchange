package pl.nn.currencyexchange.infrastucture.configuration.feign;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FeignErrorDecoder implements ErrorDecoder {

  private static final String NOT_FOUND_ERROR = "Unable to fetch data from external service";

  @Override
  public Exception decode(String methodKey, Response response) {
    FeignException exception = FeignException.errorStatus(methodKey, response);
    int status = response.status();
    if (status == 404) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_ERROR);
    }
    if (status >= 500) {
      return new RetryableException(
          response.status(),
          exception.getMessage(),
          response.request().httpMethod(),
          exception,
          (Long) null,
          response.request());
    }
    return exception;
  }
}
