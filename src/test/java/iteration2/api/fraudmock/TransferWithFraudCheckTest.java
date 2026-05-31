package iteration2.api.fraudmock;

import api.dao.TransactionDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.enums.mocks.FraudMessage;
import api.mock.FraudCheckData;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.transactions.Transaction;
import api.models.accounts.transactions.TransactionType;
import api.models.accounts.transfer.TransferMoneyResponse;
import api.models.comparison.ModelAssertions;
import api.models.fraud.DepositMoneyResponseV1;

import api.requests.steps.DataBaseSteps;
import common.annotations.APIVersion;
import common.annotations.FraudCheckMock;
import common.annotations.UserSession;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import common.storage.SessionStorage;
import iteration1.api.BaseTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {
    private static final ThreadLocal<CreateAccountResponse> ownAccountOne = new ThreadLocal<>();
    private static final ThreadLocal<CreateAccountResponse> ownAccountTwo = new ThreadLocal<>();
    private static final ThreadLocal<CreateAccountResponse> otherAccountOne = new ThreadLocal<>();

    @BeforeEach
    public void setupTest() {
        ownAccountOne.set(SessionStorage.getUserSteps(1).createAccount());
        ownAccountTwo.set(SessionStorage.getUserSteps(1).createAccount());
        this.softly = new SoftAssertions();
    }

    @Test
    @Tag("wiremock")
    @UserSession(type = "API")
    @APIVersion("with_fraud_check_with_transfer_fix")
    @FraudCheckMock(
            status = FraudCheckData.STATUS_SUCCESS,
            decision = FraudCheckData.DECISION_APPROVED,
            riskScore = FraudCheckData.FRAUD_RISK_SCORE_LOW,
            reason = FraudCheckData.FRAUD_REASON_LOW_RISK,
            requiresManualReview = FraudCheckData.REQUIRES_MANUAL_REVIEW_FALSE,
            additionalVerificationRequired = FraudCheckData.REQUIRES_VERIFICATION_FALSE
    )
    public void AuthUserCanTransferMoneyBetweenHisOwnAccountsWithFraudCheck() {
        double depositAmount = Math.random() * 4999.9 + 0.1;
        DepositMoneyResponseV1 depositResponse = SessionStorage.getUserSteps(1).depositToAccountFraud(ownAccountOne.get().getId(), depositAmount);

        ownAccountTwo.set(SessionStorage.getUserSteps(1).createAccount());

        double transferAmount = Math.random() * (depositResponse.getBalance() - 0.1) + 0.1;
        TransferMoneyResponse transferResponse = SessionStorage.getUserSteps(1).transferWithFraudCheck(
                ownAccountOne.get().getId(),
                ownAccountTwo.get().getId(),
                transferAmount
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferMoneyResponse expectedResponse = FraudCheckData
                .expectedApprovedTransfer(ownAccountOne.get().getId(), ownAccountTwo.get().getId(), transferAmount).build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

        Transaction expectedTransactionOne = Transaction.builder()
                .amount(depositResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(ownAccountTwo.get().getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.get().getId(), expectedTransactionOne);

        assertThat(transactionsAccountOne.size()).isEqualTo(1);

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransactionOne);
        DaoAndModelAssertions.assertThat(transferResponse, transactionDaoOne).match();
    }

    @Test
    @Tag("wiremock")
    @UserSession(type = "API", value = 2)
    @APIVersion("with_fraud_check_with_transfer_fix")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void AuthUserCanTransferMoneyToNotHisAccountWithFraudCheckTest() {
        ownAccountOne.set(SessionStorage.getUserSteps(1).createAccount());
        otherAccountOne.set(SessionStorage.getUserSteps(1).createAccount());

        double depositAmount = Math.random() * 4999.9 + 0.1;
        DepositMoneyResponseV1 depositResponse = SessionStorage.getUserSteps(1).depositToAccountFraud(ownAccountOne.get().getId(), depositAmount);


        double transferAmount = Math.random() * (depositResponse.getBalance() - 0.1) + 0.1;
        TransferMoneyResponse transferResponse = SessionStorage.getUserSteps(1).transferWithFraudCheck(
                ownAccountOne.get().getId(),
                otherAccountOne.get().getId(),
                transferAmount
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferMoneyResponse expectedResponse = FraudCheckData
                .expectedApprovedTransfer(ownAccountOne.get().getId(), otherAccountOne.get().getId(), transferAmount).build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

        Transaction expectedTransaction = Transaction.builder()
                .amount(depositResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(otherAccountOne.get().getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps(1)
                .getAccountTransactionsByParams(ownAccountOne.get().getId(), expectedTransaction);

        assertThat(transactionsAccountOne.size()).isEqualTo(1);

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        DaoAndModelAssertions.assertThat(transferResponse, transactionDaoOne).match();
    }

    @Test
    @Tag("wiremock")
    @UserSession(type = "API")
    @APIVersion("with_fraud_check_with_transfer_fix")
    @FraudCheckMock(
            status = FraudCheckData.STATUS_SUCCESS,
            decision = FraudCheckData.DECISION_APPROVED,
            riskScore = FraudCheckData.FRAUD_RISK_SCORE_LOW,
            reason = FraudCheckData.FRAUD_REASON_LOW_RISK,
            requiresManualReview = FraudCheckData.REQUIRES_MANUAL_REVIEW_FALSE,
            additionalVerificationRequired = FraudCheckData.REQUIRES_VERIFICATION_FALSE
    )
    public void AuthUserCannotTransferMoneyMoreThenExistTest() {
        double depositAmount = Math.random() * 4999.9 + 0.1;
        DepositMoneyResponseV1 depositMoneyResponse = SessionStorage.getUserSteps(1).depositToAccountFraud(ownAccountOne.get().getId(), depositAmount);

        TransferMoneyResponse transferResponse = SessionStorage.getUserSteps(1).transferWithFraudCheckReturnsBadRequest(
                ownAccountOne.get().getId(),
                ownAccountTwo.get().getId(),
                depositMoneyResponse.getBalance() + 1,
                FraudMessage.INSUFFICIENT_FUNDS.getMessage()
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferMoneyResponse expectedResponse = FraudCheckData
                .expectedFailTransfer(FraudMessage.INSUFFICIENT_FUNDS.getMessage()).build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

        Transaction expectedTransaction = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(ownAccountTwo.get().getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(
                        ownAccountOne.get().getId(), expectedTransaction);

        assertThat(transactionsAccountOne.size()).isZero();

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDaoOne).isNull();
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of(0.009, FraudMessage.INSUFFICIENT_FUNDS.getMessage()),
                Arguments.of(-10000, FraudMessage.INVALID_ACCOUNTS_OR_AMOUNT.getMessage()),
                Arguments.of(10000.01, FraudMessage.TRANSFER_AMOUNT_CANNOT_EXCEED_TEN_THOUSAND.getMessage())
        );
    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    @Tag("wiremock")
    @UserSession(type = "API")
    @APIVersion("with_fraud_check_with_transfer_fix")
    @FraudCheckMock(
            status = FraudCheckData.STATUS_SUCCESS,
            decision = FraudCheckData.DECISION_APPROVED,
            riskScore = FraudCheckData.FRAUD_RISK_SCORE_LOW,
            reason = FraudCheckData.FRAUD_REASON_LOW_RISK,
            requiresManualReview = FraudCheckData.REQUIRES_MANUAL_REVIEW_FALSE,
            additionalVerificationRequired = FraudCheckData.REQUIRES_VERIFICATION_FALSE
    )
    public void userCanDepositInvalidMoneyToHisOwnAccountTestV1(double amount, String errorValue) {
        TransferMoneyResponse transferResponse = SessionStorage.getUserSteps(1).transferWithFraudCheckReturnsBadRequest(
                ownAccountOne.get().getId(),
                ownAccountTwo.get().getId(),
                amount,
                errorValue
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferMoneyResponse expectedResponse = FraudCheckData
                .expectedFailTransfer(errorValue).build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

        Transaction expectedTransaction = Transaction.builder()
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .relatedAccountId(ownAccountTwo.get().getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.get().getId(), expectedTransaction);


        assertThat(transactionsAccountOne.size()).isZero();

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDaoOne).isNull();
    }

    @AfterEach
    public void afterTest() {
        ownAccountOne.remove();
        ownAccountTwo.remove();
        otherAccountOne.remove();
        softly.assertAll();
    }
}