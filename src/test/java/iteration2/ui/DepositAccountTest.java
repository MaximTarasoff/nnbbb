package iteration2.ui;

import api.generators.RandomModelGenerator;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.Transaction;
import api.models.accounts.deposit.DepositMoneyResponse;
import com.codeborne.selenide.Condition;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.enums.BankAlert;
import ui.pages.DepositMoney;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DepositAccountTest extends BaseUiTest {
    private double randomAmount;
    private CreateAccountResponse ownAccount;
    private CreateAccountResponse otherAccount;

    @BeforeEach
    public void setUp() {
        randomAmount = RandomModelGenerator.generate(DepositMoneyResponse.class).getBalance();
        ownAccount = SessionStorage.getUserSteps().createAccount();

        new DepositMoney()
                .open()
                .getDepositMoneyTitleText()
                .shouldBe(Condition.visible);
    }

    @Test
    @UserSession
    public void userCanDepositMoneyToHisOwnAccountTest() {
        new DepositMoney()
                .selectAccountInListByName(ownAccount.getAccountNumber())
                .enterAmount(randomAmount)
                .depositMoney()
                .checkAlertMessageAndAccept(String.format(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage(), randomAmount, ownAccount.getAccountNumber()));

        List<CreateAccountResponse> createdAccounts = SessionStorage.getUserSteps().getAllAccounts();
        assertThat(createdAccounts.getFirst().getBalance()).isEqualTo(randomAmount);

        List<Transaction> depositTransaction = SessionStorage.getUserSteps().getAllAccountTransactions(ownAccount.getId())
                .stream().filter(transaction -> transaction.getType().contains("DEPOSIT") &&
                        transaction.getAmount() == randomAmount &&
                        transaction.getRelatedAccountId() == ownAccount.getId())
                .toList();
        assertThat(depositTransaction.size()).isEqualTo(1);
    }

    @Test
    @UserSession(value = 2)
    public void userCannotDepositMoneyToNotHisAccountTest() {
        otherAccount = SessionStorage.getUserSteps(2).createAccount();

        boolean isOtherAccountInList = new DepositMoney().getAllAccounts()
                .stream().anyMatch(account -> account.getAccountName().equals(otherAccount.getAccountNumber()));

        assertFalse(isOtherAccountInList);
    }

    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(
                Arguments.of(0.009, "❌ Failed to deposit. Please try again."),
                Arguments.of(-5000, "❌ Please enter a valid amount."),
                Arguments.of(5000.001, "❌ Please deposit less or equal to 5000$.")
        );
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest(name = "Deposit {0} should return error: {1}")
    @UserSession
    public void userCanDepositInvalidMoneyToHisOwnAccountTest(double balance, String errorValue) {
        new DepositMoney()
                .selectAccountInListByName(ownAccount.getAccountNumber())
                .enterAmount(balance)
                .depositMoney()
                .checkAlertMessageAndAccept(errorValue);

        List<Transaction> transactions = SessionStorage.getUserSteps().getAllAccountTransactions(ownAccount.getId())
                .stream().filter(transaction -> transaction.getType().contains("DEPOSIT") &&
                        transaction.getAmount() == balance &&
                        transaction.getRelatedAccountId() == ownAccount.getId())
                .toList();

        assertThat(transactions.size()).isZero();
    }

    @AfterEach
    public void tearDown() {
        SessionStorage.clear();
    }
}
