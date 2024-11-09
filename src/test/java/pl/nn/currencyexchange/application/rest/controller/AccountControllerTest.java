package pl.nn.currencyexchange.application.rest.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.nn.currencyexchange.application.rest.dto.AccountBalanceQuery;
import pl.nn.currencyexchange.configuration.WireMockConfig;
import pl.nn.currencyexchange.domain.enums.Currency;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {WireMockConfig.class})
class AccountControllerTest {

  private static final String DATA_PATH = "/json/";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WireMockServer wireMockServer;

  @BeforeEach
  void setUp() {
    wireMockServer.start();
  }

  @Test
  void shouldSuccessfullyCreateAccount() throws Exception {
    // when
    var request = post("/account/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(getFileContent("createAccount__validRequest.json"));

    // then
    mockMvc.perform(request)
        .andExpect(status().isCreated());
  }

  @Test
  void shouldHandleExceptionForToSmallAmountDuringCreateAccount() throws Exception {
    // when
    var request = post("/account/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(getFileContent("createAccount__toSmallInitialAmount.json"));

    // then
    mockMvc.perform(request)
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldHandleExceptionForEmptyFirstNameDuringCreateAccount() throws Exception {
    // when
    var request = post("/account/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(getFileContent("createAccount__emptyValues.json"));

    // then
    mockMvc.perform(request)
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldSuccessfullyGetAccountData() throws Exception {
    // given
    var expectedResult = AccountBalanceQuery.builder()
        .owner(AccountBalanceQuery.Owner.builder()
            .firstName("Dawid")
            .lastName("Testowy")
            .build())
        .wallet(List.of(
            AccountBalanceQuery.CurrencyBalance.builder()
                .amount(new BigDecimal("100.00"))
                .currency(Currency.PLN)
                .build()
        ))
        .build();

    var createAccountRequest = post("/account/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(getFileContent("createAccount__validRequest.json"));

    var createAccountResponse =
        mockMvc.perform(createAccountRequest)
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();

    var accountId = UUID.fromString(createAccountResponse.getContentAsString().replace("\"", ""));

    var getAccountBalanceRequest = get("/account/" + accountId + "/balance");

    // when
    var getAccountBalanceResponse = mockMvc.perform(getAccountBalanceRequest)
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    // then
    var responseBody = OBJECT_MAPPER.readValue(getAccountBalanceResponse.getContentAsString(),
        AccountBalanceQuery.class);

    assertThat(responseBody).isEqualTo(expectedResult);
  }

  @Test
  void shouldHandleExceptionForRetrieveNonExistingAccountData() throws Exception {
    // when
    var nonExistingAccountId = UUID.randomUUID();
    var request = get("/account/" + nonExistingAccountId + "/balance");

    // then
    mockMvc.perform(request)
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldHandleExceptionWhileExchangeMoneyForNonExistingAccount() throws Exception {
    // when
    var nonExistingAccountId = UUID.randomUUID();
    var request = post("/account/" + nonExistingAccountId + "/exchange")
        .contentType(MediaType.APPLICATION_JSON)
        .content(getFileContent("exchangeMoney__validRequest.json"));

    // then
    mockMvc.perform(request)
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldHandleExceptionForUnknownCurrency() throws Exception {
    // given
    wireMockServer.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(
            urlEqualTo("/exchangerates/rates/C/UNKNOWN"))
        .willReturn(aResponse()
            .withStatus(404)));

    var createAccountRequest = post("/account/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(getFileContent("createAccount__validRequest.json"));

    var createAccountResponse =
        mockMvc.perform(createAccountRequest)
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();

    var accountId = UUID.fromString(createAccountResponse.getContentAsString().replace("\"", ""));

    // when
    var request = post("/account/" + accountId + "/exchange")
        .contentType(MediaType.APPLICATION_JSON)
        .content(getFileContent("exchangeMoney__tooMuchTargetAmount.json"));
    ;

    // then
    mockMvc.perform(request)
        .andExpect(status().isNotFound());
  }

  private String getFileContent(String fileName) throws IOException {
    try (InputStream inputStream
             = new ClassPathResource(DATA_PATH + fileName).getInputStream()) {
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
  }
}