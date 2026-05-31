package iteration2.ui;

import api.dao.TransactionDao;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.transactions.Transaction;
import api.models.accounts.transactions.TransactionType;
import api.requests.steps.DataBaseSteps;
import com.codeborne.selenide.Condition;
import common.annotations.APIVersion;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import api.models.accounts.deposit.DepositMoneyResponseV2;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.enums.BankAlert;
import ui.pages.TransferPage;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class TransferAccountTest extends BaseUiTest {

    @Test
    @UserSession
    @APIVersion("with_database_with_fix")
    public void AuthUserCanTransferMoneyBetweenHisOwnAccountsTest() {
        CreateAccountResponse ownAccountOne = SessionStorage.getUserSteps().createAccount();
        CreateAccountResponse ownAccountTwo = SessionStorage.getUserSteps().createAccount();
        DepositMoneyResponseV2 depositMoneyResponse = SessionStorage.getUserSteps().depositRandomBalanceToAccount(ownAccountOne.getId());

        new TransferPage()
                .open()
                .getTransferTitleText()
                .shouldBe(Condition.visible);

        new TransferPage()
                .selectAccountInListByName(ownAccountOne.getAccountNumber())
                .enterRecipientAccountNumber(ownAccountTwo.getAccountNumber())
                .enterAmount(depositMoneyResponse.getBalance())
                .setCheckBox()
                .sendTransfer()
                .checkAlertMessageAndAccept(String.format(BankAlert.TRANSFER_SUCCESSFULLY.getMessage(), depositMoneyResponse.getBalance(), ownAccountTwo.getAccountNumber()));

        Transaction expectedTransactionOne = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(ownAccountTwo.getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.getId(), expectedTransactionOne);

        assertThat(transactionsAccountOne.size()).isEqualTo(1);

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransactionOne);
        softly.assertThat(transactionDaoOne.getRelatedAccountId()).isEqualTo(expectedTransactionOne.getRelatedAccountId());
        softly.assertThat(transactionDaoOne.getAmount()).isEqualTo(expectedTransactionOne.getAmount());

        Transaction expectedTransactionTwo = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_IN)
                .relatedAccountId(ownAccountOne.getId())
                .build();

        List<Transaction> transactionsAccountTwo = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(
                        ownAccountTwo.getId(), expectedTransactionTwo);

        assertThat(transactionsAccountTwo.size()).isEqualTo(1);

        TransactionDao transactionDaoTwo = DataBaseSteps.getTransactionByTransaction(expectedTransactionTwo);
        softly.assertThat(transactionDaoTwo.getRelatedAccountId()).isEqualTo(expectedTransactionTwo.getRelatedAccountId());
        softly.assertThat(transactionDaoTwo.getAmount()).isEqualTo(expectedTransactionTwo.getAmount());
    }

    @Test
    @UserSession(value = 2)
    @APIVersion("with_database_with_fix")
    public void AuthUserCanTransferMoneyToNotHisAccountTest() {
        CreateAccountResponse ownAccount = SessionStorage.getUserSteps(1).createAccount();
        CreateAccountResponse otherAccount = SessionStorage.getUserSteps(2).createAccount();

        DepositMoneyResponseV2 depositMoneyResponse = SessionStorage.getUserSteps().depositRandomBalanceToAccount(ownAccount.getId());

        new TransferPage()
                .open()
                .getTransferTitleText()
                .shouldBe(Condition.visible);

        new TransferPage()
                .selectAccountInListByName(ownAccount.getAccountNumber())
                .enterRecipientAccountNumber(otherAccount.getAccountNumber())
                .enterAmount(depositMoneyResponse.getBalance())
                .setCheckBox()
                .sendTransfer()
                .checkAlertMessageAndAccept(String.format(BankAlert.TRANSFER_SUCCESSFULLY.getMessage(), depositMoneyResponse.getBalance(), otherAccount.getAccountNumber()));

        Transaction expectedTransactionOne = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(otherAccount.getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps(1)
                .getAccountTransactionsByParams(ownAccount.getId(), expectedTransactionOne);

        assertThat(transactionsAccountOne.size()).isEqualTo(1);

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransactionOne);
        softly.assertThat(transactionDaoOne.getRelatedAccountId()).isEqualTo(expectedTransactionOne.getRelatedAccountId());
        softly.assertThat(transactionDaoOne.getAmount()).isEqualTo(expectedTransactionOne.getAmount());

        Transaction expectedTransactionTwo = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_IN)
                .relatedAccountId(ownAccount.getId())
                .build();

        List<Transaction> transactionsAccountTwo = SessionStorage.getUserSteps(2)
                .getAccountTransactionsByParams(otherAccount.getId(), expectedTransactionTwo);

        assertThat(transactionsAccountTwo.size()).isEqualTo(1);

        TransactionDao transactionDaoTwo = DataBaseSteps.getTransactionByTransaction(expectedTransactionTwo);
        softly.assertThat(transactionDaoTwo.getRelatedAccountId()).isEqualTo(expectedTransactionTwo.getRelatedAccountId());
        softly.assertThat(transactionDaoTwo.getAmount()).isEqualTo(expectedTransactionTwo.getAmount());

    }

    @Test
    @UserSession
    @APIVersion("with_database_with_fix")
    public void AuthUserCannotTransferMoneyMoreThenExistTest() {
        CreateAccountResponse ownAccountOne = SessionStorage.getUserSteps().createAccount();
        CreateAccountResponse ownAccountTwo = SessionStorage.getUserSteps().createAccount();
        DepositMoneyResponseV2 depositMoneyResponse = SessionStorage.getUserSteps().depositRandomBalanceToAccount(ownAccountOne.getId());

        new TransferPage()
                .open()
                .getTransferTitleText()
                .shouldBe(Condition.visible);

        new TransferPage()
                .selectAccountInListByName(ownAccountOne.getAccountNumber())
                .enterRecipientAccountNumber(ownAccountTwo.getAccountNumber())
                .enterAmount(depositMoneyResponse.getBalance() + 1)
                .setCheckBox()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_INVALID_MONEY_AMOUNT.getMessage());

        Transaction expectedTransactionOne = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(ownAccountTwo.getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(
                        ownAccountOne.getId(), expectedTransactionOne);

        assertThat(transactionsAccountOne.size()).isZero();

        Transaction expectedTransactionTwo = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_IN)
                .relatedAccountId(ownAccountOne.getId())
                .build();

        List<Transaction> transactionsAccountTwo = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(
                        ownAccountTwo.getId(), expectedTransactionTwo);

        assertThat(transactionsAccountTwo.size()).isZero();

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransactionOne);
        TransactionDao transactionDaoTwo = DataBaseSteps.getTransactionByTransaction(expectedTransactionTwo);
        softly.assertThat(transactionDaoOne).isNull();
        softly.assertThat(transactionDaoTwo).isNull();

    }

    public static Stream<Arguments> transferInvalidDataV1() {
        return Stream.of(
                Arguments.of(0.009, "Transfer amount must be at least 0.01"),
                Arguments.of(-10000, "Transfer amount must be at least 0.01"),
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000")
        );
    }

    @MethodSource("transferInvalidDataV1")
    @ParameterizedTest
    @UserSession
    @APIVersion("with_validation_fix")
    public void userCanDepositInvalidMoneyToHisOwnAccountTestV1(double amount, String errorValue) {
        CreateAccountResponse ownAccountOne = SessionStorage.getUserSteps().createAccount();
        CreateAccountResponse ownAccountTwo = SessionStorage.getUserSteps().createAccount();

        new TransferPage()
                .open()
                .getTransferTitleText()
                .shouldBe(Condition.visible);

        new TransferPage()
                .selectAccountInListByName(ownAccountOne.getAccountNumber())
                .enterRecipientAccountNumber(ownAccountTwo.getAccountNumber())
                .enterAmount(amount)
                .setCheckBox()
                .sendTransfer()
                .checkAlertMessageAndAccept(errorValue);

        Transaction expectedTransaction = Transaction.builder()
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .relatedAccountId(ownAccountTwo.getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.getId(), expectedTransaction);

        assertThat(transactionsAccountOne.size()).isZero();

        TransactionDao transactionDao = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDao).isNull();
    }

    public static Stream<Arguments> transferInvalidDataV2() {
        return Stream.of(
                Arguments.of(0.009, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(-10000, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000")
        );
    }

    @MethodSource("transferInvalidDataV2")
    @ParameterizedTest
    @UserSession
    @APIVersion("with_database_with_fix")
    public void userCanDepositInvalidMoneyToHisOwnAccountTestV2(double amount, String errorValue) {
        CreateAccountResponse ownAccountOne = SessionStorage.getUserSteps().createAccount();
        CreateAccountResponse ownAccountTwo = SessionStorage.getUserSteps().createAccount();

        new TransferPage()
                .open()
                .getTransferTitleText()
                .shouldBe(Condition.visible);

        new TransferPage()
                .selectAccountInListByName(ownAccountOne.getAccountNumber())
                .enterRecipientAccountNumber(ownAccountTwo.getAccountNumber())
                .enterAmount(amount)
                .setCheckBox()
                .sendTransfer()
                .checkAlertMessageAndAccept(errorValue);

        Transaction expectedTransaction = Transaction.builder()
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .relatedAccountId(ownAccountTwo.getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.getId(), expectedTransaction);

        assertThat(transactionsAccountOne.size()).isZero();

        TransactionDao transactionDao = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDao).isNull();
    }

    @AfterEach
    public void tearDown() {
        SessionStorage.clear();
    }
}
