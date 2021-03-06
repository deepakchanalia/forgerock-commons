/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/*global require, define, QUnit */

define([
    "org/forgerock/commons/ui/common/main/EventManager",
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/mock/ui/common/main/LocalStorage",
    "org/forgerock/commons/ui/common/main/Configuration",
    "org/forgerock/commons/ui/common/main/Router",
    "./getLoggedUser"
], function (eventManager, constants, localStorage, conf, router, getLoggedUser) {
    return {
        executeAll: function (server, parameters) {

            var userRegPromise = $.Deferred(),
                rememberPromise = $.Deferred(),
                securityDataPromise = $.Deferred();

            module('Mock Tests');

            QUnit.asyncTest('User Registration', function () {
                conf.loggedUser = null;

                var view = require('RegisterView');
                view.element = $("<div>")[0];

                delete view.route;

                view.render(null, function () {
                    // fields
                    var register = $('input[name="register"]', view.$el),
                        userName = $('input[name="userName"]', view.$el),
                        mail = $('input[name="mail"]', view.$el),
                        givenName = $('input[name="givenName"]', view.$el),
                        lastName = $('input[name="sn"]', view.$el),
                        phone = $('input[name="telephoneNumber"]', view.$el),
                        terms = $('input[name="terms"]', view.$el),
                        pwd = $('input[name="password"]', view.$el),
                        pwdConfirm = $('input[name="passwordConfirm"]', view.$el),
                    // password validation messages
                        pwdConfirmMatchesPwd = $('[data-for-validator="passwordConfirm"]', view.$el),
                        pwdRequired = $('[data-for-req="REQUIRED"]', view.$el),
                        pwdContainsNumbers = $('[data-for-req="AT_LEAST_X_NUMBERS"]', view.$el),
                        pwdMinLength = $('[data-for-req="MIN_LENGTH"]', view.$el),
                        pwdContainsCapitalLetters = $('[data-for-req="AT_LEAST_X_CAPITAL_LETTERS"]', view.$el);


                    function testStatusAttr(field, validationStatus) {
                        QUnit.equal(field.attr('data-validation-status'), validationStatus,
                            'Validation status for ' + field.attr('name') + ' is "' + validationStatus + '" for value "' + field.val() + '"');
                    }

                    function testValue(field, value, expectedValidationMsg, validationStatus, view) {
                        field.val(value).trigger('change');
                        field.trigger('hover');
                        var fieldName = field.attr('name'),
                            validationMsg = (field.data('bs.popover')) ? $("<div>" + field.data('bs.popover').options.content + "</div>").text().trim() : "";
                        QUnit.equal(validationMsg, expectedValidationMsg,
                            'Message "' + expectedValidationMsg + '" is displayed for ' + fieldName + ' for value "' + value + '"');
                        testStatusAttr(field, validationStatus);
                    }

                    function testFieldRule(field, val, rule, ruleName, status) {
                        field.val(val).trigger('change');
                        QUnit.ok(status === 'ok' ? rule.parent().find('.ok').length === 1 : rule.parent().find('.error').length === 1,
                            '"' + ruleName + '" rule has "' + status + '" status for value "' + val + '"');
                    }

                    // initial state
                    QUnit.equal($('input[name="register"]', view.$el).prop('disabled'), true, 'Initial state of submit button is disabled');

                    testStatusAttr(userName, 'error');
                    testStatusAttr(mail, 'error');
                    testStatusAttr(givenName, 'error');
                    testStatusAttr(lastName, 'error');
                    testStatusAttr(phone, 'ok'); // phone is optional
                    testStatusAttr(pwd, 'error');
                    testStatusAttr(pwdConfirm, 'error');
                    testStatusAttr(terms, 'error');

                    QUnit.ok(!terms.is(':checked'), 'Terms of use not checked by default');

                    // password
                    testFieldRule(pwd, 'abc', pwdRequired, pwdRequired.attr('data-for-req'), 'ok');
                    testFieldRule(pwd, 'abc1', pwdContainsNumbers, pwdContainsNumbers.attr('data-for-req'), 'ok');
                    testFieldRule(pwd, 'abcdefg1', pwdMinLength, pwdMinLength.attr('data-for-req'), 'ok');
                    testFieldRule(pwd, 'Abcdefg1', pwdContainsCapitalLetters, pwdContainsCapitalLetters.attr('data-for-req'), 'ok');

                    testFieldRule(pwdConfirm, 'abcdefg1', pwdConfirmMatchesPwd, pwdConfirmMatchesPwd.attr('data-for-validator'), 'error');
                    testFieldRule(pwdConfirm, 'Abcdefg1', pwdConfirmMatchesPwd, pwdConfirmMatchesPwd.attr('data-for-validator'), 'ok');

                    testStatusAttr(pwd, 'ok');
                    testStatusAttr(pwdConfirm, 'ok');

                    // username
                    testValue(userName, '', 'Cannot be blank', 'error', view);
                    testValue(userName, 'qqq', '', 'ok', view);

                    // e-mail
                    testValue(mail, '', 'Cannot be blank', 'error', view);
                    testValue(mail, 'abc', 'Must be a valid email address', 'error', view);
                    testValue(mail, 'abc@', 'Must be a valid email address', 'error', view);
                    testValue(mail, 'abc@qqq', 'Must be a valid email address', 'error', view);
                    testValue(mail, 'abc@qqq.', 'Must be a valid email address', 'error', view);
                    testValue(mail, 'abc@qqq.com', '', 'ok', view);

                    // given name
                    testValue(givenName, '', 'Cannot be blank', 'error', view);
                    testValue(givenName, 'abc', '', 'ok', view);

                    // last name
                    testValue(lastName, '', 'Cannot be blank', 'error', view);
                    testValue(lastName, 'qqq', '', 'ok', view);

                    // phone
                    testValue(phone, 'abc', 'Must be a valid phone number', 'error', view);
                    testValue(phone, '12345', '', 'ok', view);

                    // terms of use
                    terms.prop('checked', true).trigger('blur');
                    QUnit.equal($('.validation-message-checkbox').text(), '', '"Acceptance required for registration" is not shown for checked checkbox');
                    testStatusAttr(terms, 'ok');

                    // register button
                    QUnit.ok(register.prop('disabled') === false, 'Register button is enabled for correct field values');

                    register.trigger('click');
                    QUnit.ok(conf.loggedUser && conf.loggedUser.userName === "qqq", 'Logged in with newly created user');

                    QUnit.start();

                });
            });

            QUnit.asyncTest('Navigation', function () {
                var moduleClass     = "org/forgerock/commons/ui/common/components/Navigation",
                    navigationView  = require(moduleClass),
                    configuration   = _.find(conf.appConfiguration.moduleDefinition, function(module){
                                         return module.moduleClass === moduleClass;
                                    }).configuration,
                    navDropdownMenu = null,
                    securityLink = null;

                conf.loggedUser = {"userName": "test"};

                $("#qunit-fixture").append("<div id='menu'></div>");

                navigationView.init(function(){
                    securityLink = $('#security_link');
                    navDropdownMenu = $('#navDropdownMenu li');

                    QUnit.ok(navDropdownMenu.length > 0 && navDropdownMenu.length === configuration.userBar.length, "All UserBar items displayed (" + navDropdownMenu.length + ")");
                    QUnit.ok(securityLink.data().event && securityLink.data().event === constants.EVENT_SHOW_CHANGE_SECURITY_DIALOG, "Security link has event in data attribute");
                    QUnit.start();
                });


            });

            QUnit.asyncTest('Change Security Data', function () {
                conf.loggedUser = getLoggedUser();

                var changeDataView = require("ChangeSecurityDataDialog"),
                    stub = sinon.stub(changeDataView, "render", function (args,callback) {
                        changeDataView.render.restore();
                        changeDataView.render([],function (modal) {
                            var dialog = modal.$modal,
                                pwd = $('input[name="password"]', dialog),
                                pwdConfirm = $('input[name="passwordConfirm"]', dialog);
    
                            // Test if inputs and submit button are available
                            QUnit.ok($('input[name="password"]', dialog).length > 0, "Password field is available");
                            QUnit.ok($('input[name="passwordConfirm"]', dialog).length > 0, "Password confirm field is available");
                            QUnit.ok($('#btnOk', dialog).length > 0, "Submit button is available");
    
                            // Check submit button initial status
                            QUnit.ok($('#btnOk', dialog).prop('disabled'), 'Initial state of submit button is disabled');
    
                            // Check if inputs pass validation
                            QUnit.equal($('input[name="password"]', dialog).data('validation-status'), 'error', "Empty password field doesn't pass validation");
                            QUnit.equal($('input[name="passwordConfirm"]', dialog).data('validation-status'), 'error', "Empty password confirm field doesn't pass validation");
    
                            var passwordConfirmMatchesPassword = $('[data-for-validator="passwordConfirm"]', dialog).parent(),
                                passwordRequired = $('[data-for-req="REQUIRED"]', dialog).parent(),
                                passwordContainsNumbers = $('[data-for-req="AT_LEAST_X_NUMBERS"]', dialog).parent(),
                                passwordMinLength = $('[data-for-req="MIN_LENGTH"]', dialog).parent(),
                                passwordContainsCapitalLetters = $('[data-for-req="AT_LEAST_X_CAPITAL_LETTERS"]', dialog).parent();
    
                            pwd.val('abc').trigger('change');
                            QUnit.ok(passwordRequired.find("span.error").length === 0, 'Password field cannot be blank');
    
                            pwd.val('abc1').trigger('change');
                            QUnit.ok(passwordContainsNumbers.find("span.error").length === 0, 'Number added to password satisfied AT_LEAST_X_NUMBERS');
    
                            pwd.val('abcdefgh').trigger('change');
                            QUnit.ok(passwordMinLength.find("span.error").length === 0, 'Password length satisfied MIN_LENGTH');
    
                            pwd.val('abCdefgh').trigger('change');
                            QUnit.ok(passwordContainsCapitalLetters.find("span.error").length === 0, 'Capital letter added to password satisfied AT_LEAST_X_CAPITAL_LETTERS');
    
                            pwd.val('1122334455').trigger('change');
                            QUnit.equal($('input[name="password"]', dialog).data('validation-status'), 'error', "Password doesn't pass validation");
    
                            pwd.val('apple').trigger('change');
                            pwdConfirm.val('apple').trigger('change');
                            QUnit.equal($('input[name="passwordConfirm"]', dialog).attr('data-validation-status'), 'error', 'Password not confirming with bad values');
    
                            pwd.val('Passw0rd').trigger('change');
                            pwdConfirm.val('Passw0rd').trigger('change');
                            QUnit.equal($('input[name="password"]', dialog).attr('data-validation-status'), 'ok', 'Password passes validation');
                            QUnit.equal($('input[name="passwordConfirm"]', dialog).attr('data-validation-status'), 'ok', 'Password confirm field passes validation');
                            QUnit.ok(passwordConfirmMatchesPassword.find("span.error").length === 0, 'Confirmation matches password');
                            QUnit.ok(!$('#btnOk', dialog).prop('disabled'), 'Submit button is enabled');
    
                            // Check if new password was set for user
                            pwd.val('Passw0rds').trigger('change');
                            pwdConfirm.val('Passw0rds').trigger('change');
                            $('#btnOk', dialog).trigger("click");
                            QUnit.ok(conf.loggedUser !== undefined, "User should be logged in");
                            QUnit.ok(conf.loggedUser.password == 'Passw0rds', "New password wasn't set for the user");
    
                            // log-out
                            localStorage.remove('mock/repo/internal/user/test');
                            conf.setProperty('loggedUser', null);

                            QUnit.start();
                        });
                    });

                    
                window.location.hash = "profile/change_security_data/";
            });

            QUnit.asyncTest("Unauthorized non-GET Request", function () {
                var loginDialog = require("LoginDialog"),
                    loginDialogSpy = sinon.spy(loginDialog, 'render');

                QUnit.ok(!loginDialogSpy.called, "Login Dialog render function has not yet been called");
                conf.loggedUser = getLoggedUser();
                delete conf.globalData.authorizationFailurePending;
                eventManager.sendEvent(constants.EVENT_CHANGE_VIEW, {
                    route: router.configuration.routes.profile,
                    callback: function () {
                        eventManager.sendEvent(constants.EVENT_UNAUTHORIZED, {error: {type:"POST"} });
                        QUnit.ok(conf.loggedUser !== null, "User info should be retained after UNAUTHORIZED POST error");
                        QUnit.ok(loginDialogSpy.called, "Login Dialog render function was called");
                        QUnit.start();
                    }
                });
            });

            QUnit.asyncTest("Unauthorized GET Request", function () {
                conf.loggedUser = getLoggedUser();
                delete conf.globalData.authorizationFailurePending;
                delete conf.gotoURL;

                var changeDataView = require("ChangeSecurityDataDialog"),
                    stub = sinon.stub(changeDataView, "render", function (args,callback) {
                        changeDataView.render.restore();
                        changeDataView.render([],function (modal) {
                            eventManager.sendEvent(constants.EVENT_UNAUTHORIZED, {error: {type:"GET"} });
                            QUnit.ok(conf.loggedUser === null, "User info should be discarded after UNAUTHORIZED GET error");
                            QUnit.ok(conf.gotoURL === "#profile/change_security_data/", "gotoURL should be set to change security data after UNAUTHORIZED GET error");
                            QUnit.start();
                        });
                    });
                
                window.location.hash = "profile/change_security_data/";
            });

            QUnit.asyncTest("Opening a dialog with a parameterized base view (CUI-58)", function () {
                var selfReg = require("RegisterView"),
                    stub = sinon.stub(selfReg, "render", function (args, callback) {
                        selfReg.render.restore();
                        selfReg.render(args, function () {
                            QUnit.ok(args[0] === "/foo" && args[1] === "&bar=blah", "Dialog route triggers base view to render with expected arg value");
                            QUnit.start();
                        });
                    });

                window.location.hash = "registerTerms/foo&bar=blah";
            });

            QUnit.asyncTest("Loading a module asynchronously (CUI-62)", function () {

                QUnit.equal(typeof $.prototype.jqGrid, "undefined", "jqGrid not loaded");

                require("org/forgerock/commons/ui/common/util/ModuleLoader").load("jqgrid").then(function () {
                    QUnit.equal(typeof $.prototype.jqGrid, "function", "jqGrid loaded by module loader");
                    QUnit.start();
                });

            });
        }
    };
});
