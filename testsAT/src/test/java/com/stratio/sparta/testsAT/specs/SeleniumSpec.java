/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.testsAT.specs;

import com.stratio.qa.cucumber.converter.ArrayListConverter;
import com.stratio.qa.cucumber.converter.NullableStringConverter;
import com.stratio.qa.specs.BaseGSpec;
import com.stratio.qa.specs.CommonG;
import com.stratio.qa.utils.PreviousWebElements;
import com.stratio.qa.utils.ThreadProperty;
import cucumber.api.Transform;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.stratio.qa.assertions.Assertions.assertThat;

/**
 * Generic Selenium Specs.
 */
public class SeleniumSpec extends BaseGSpec {

    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public SeleniumSpec(CommonG spec) {
        this.commonspec = spec;

    }

    /**
     * Browse to {@code url} using the current browser.
     *
     * @param path path of running app
     * @throws Exception exception
     */

    /**
     * Type a {@code text} on an numbered {@code index} previously found element.
     *
     * @param text
     * @param index
     */
    @When("^I type element with variable '(.+?)' on the element on index '(\\d+?)'$")
    public void seleniumType(@Transform(NullableStringConverter.class) String text, Integer index) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
            Actions actions = new Actions(commonspec.getDriver());
                actions.moveToElement(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));
                actions.click();
                actions.sendKeys(text);
                actions.build().perform();
    }
}