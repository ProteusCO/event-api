/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package net.venturetech.event_api.profile.ui.user;

import net.venturetech.event_api.profile.model.ProfileDAO;
import net.venturetech.event_api.profile.model.company.Company;
import net.venturetech.event_api.profile.model.user.User;
import net.venturetech.event_api.profile.service.MembershipOperationProvider;
import net.venturetech.event_api.profile.service.ProfileUIService;
import net.venturetech.event_api.profile.ui.ApplicationFunctions;
import net.venturetech.event_api.support.service.AppUtil;
import net.venturetech.event_api.support.service.ApplicationFunctionPermissionCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.proteusframework.internet.http.Request;
import net.proteusframework.ui.management.ApplicationRegistry;
import net.proteusframework.ui.management.link.RegisteredLinkDAO;
import net.proteusframework.users.model.Principal;

/**
 * Service to define permission check methods for User Management
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/4/17
 */
@Service
public class UserManagementPermissionCheck implements ApplicationFunctionPermissionCheck
{
    @Autowired private ProfileUIService _uiService;
    @Autowired private ProfileDAO _profileDAO;
    @Autowired private MembershipOperationProvider _mop;
    @Autowired private RegisteredLinkDAO _registeredLinkDAO;
    @Autowired private ApplicationRegistry _applicationRegistry;
    @Autowired private AppUtil _appUtil;

    @Override
    public boolean checkPermissions(@Nonnull Request request, @Nullable User user)
    {
        if(functionExists(_appUtil.getSite(), request, _applicationRegistry, _registeredLinkDAO))
        {
            if (user == null) return false;
            Company selectedCompany = _uiService.getSelectedCompany();
            return _profileDAO.canOperate(user, selectedCompany, AppUtil.UTC, _mop.viewUser());
        }
        return false;
    }

    @Override
    public boolean checkPermissions(@Nonnull Request request, @Nullable Principal principal)
    {
        return false; //We always just use the User.
    }

    @Override
    public String getApplicationFunctionName()
    {
        return ApplicationFunctions.User.MANAGEMENT;
    }
}
