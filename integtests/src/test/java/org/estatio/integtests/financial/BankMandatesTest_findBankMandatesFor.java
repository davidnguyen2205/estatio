/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.integtests.financial;

import java.util.List;
import javax.inject.Inject;
import org.estatio.dom.financial.*;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.financial.BankAccountAndMandateForTopModel;
import org.estatio.integtests.EstatioIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.fixturescripts.FixtureScript;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BankMandatesTest_findBankMandatesFor extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        scenarioExecution().install(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                execute(new EstatioBaseLineFixture(), executionContext);

                execute(new BankAccountAndMandateForTopModel(), executionContext);
            }
        });
    }

    @Test
    public void forAccountWithMandate() {
        // given
        FinancialAccount account = financialAccounts.findAccountByReference(BankAccountAndMandateForTopModel.BANK_ACCOUNT_REF);
        Assert.assertThat(account instanceof BankAccount, is(true));
        final BankAccount bankAccount = (BankAccount) account;

        // when
        List<BankMandate> mandates = bankMandates.findBankMandatesFor(bankAccount);

        // then
        assertThat(mandates.size(), is(1));
    }


    @Inject
    private FinancialAccounts financialAccounts;
    @Inject
    private BankMandates bankMandates;

}