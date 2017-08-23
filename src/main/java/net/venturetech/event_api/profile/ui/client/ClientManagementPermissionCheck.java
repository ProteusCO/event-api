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

package net.venturetech.event_api.profile.ui.client;

import net.venturetech.event_api.profile.model.ProfileDAO;
import net.venturetech.event_api.profile.model.company.Company;
import net.venturetech.event_api.profile.model.user.User;
import net.venturetech.event_api.profile.model.user.UserDAO;
import net.venturetech.event_api.profile.service.MembershipOperationConfiguration;
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
 * Provides methods for checking permission to view the Client Management Pages
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/12/17
 */
@Service
public class ClientManagementPermissionCheck implements ApplicationFunctionPermissionCheck
{
    @Autowired private ProfileUIService _uiService;
    @Autowired private ProfileDAO _profileDAO;
    @Autowired private MembershipOperationConfiguration _mop;
    @Autowired private RegisteredLinkDAO _registeredLinkDAO;
    @Autowired private ApplicationRegistry _applicationRegistry;
    @Autowired private UserDAO _userDAO;
    @Autowired private AppUtil _appUtil;

    @Override
    public boolean checkPermissions(@Nonnull Request request, @Nullable User user)
    {
        if(functionExists(_appUtil.getSite(), request, _applicationRegistry, _registeredLinkDAO))
        {
            Company company = _uiService.getSelectedCompany();

            return _profileDAO.canOperate(user, company, AppUtil.UTC, _mop.viewClient()) || checkCanUserModify(request, user);
        }
        return false;
    }

    @Override
    public boolean checkPermissions(@Nonnull Request request, @Nullable Principal principal)
    {
        return false; //We just use the User
    }

    @Override
    public String getApplicationFunctionName()
    {
        return ApplicationFunctions.Client.MANAGEMENT;
    }

    /**
     * Check if the given User can modify Clients for the selected Company
     *
     * @param request the request
     * @param user the user
     *
     * @return true if the given user has permission to modify Clients
     */
    public boolean checkCanUserModify(@Nonnull Request request, @Nullable User user)
    {
        if(functionExists(_appUtil.getSite(), request, _applicationRegistry, _registeredLinkDAO))
        {
            Company company = _uiService.getSelectedCompany();

            return _profileDAO.canOperate(user, company, AppUtil.UTC, _mop.modifyClient());
        }
        return false;
    }

    /**
     * Check if the current User can modify Clients for the selected Company
     *
     * @param request the request
     *
     * @return true if the current user has permission to modify Clients
     */
    public boolean checkCanCurrentUserModify(@Nonnull Request request)
    {
        return checkCanUserModify(request, _userDAO.getAssertedCurrentUser());
    }
}
