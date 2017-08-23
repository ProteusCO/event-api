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

package net.venturetech.event_api.profile.ui.company.resource;

import net.venturetech.event_api.profile.model.Profile;
import net.venturetech.event_api.profile.model.repository.Repository;
import net.venturetech.event_api.profile.model.repository.ResourceRepositoryItem;
import net.venturetech.event_api.profile.model.resource.ResourceType;
import net.venturetech.event_api.profile.ui.ApplicationFunctions;
import net.venturetech.event_api.profile.ui.URLConfigurations;
import net.venturetech.event_api.profile.ui.URLProperties;
import net.venturetech.event_api.profile.ui.resource.AbstractProfileResourcePropertyEditor;
import net.venturetech.event_api.profile.ui.resource.ResourceTypeURLConfigPropertyConverter;
import net.venturetech.event_api.support.ui.Application;
import org.springframework.beans.factory.annotation.Autowired;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;

import static net.venturetech.event_api.profile.ui.company.resource.CompanyResourcePropertyEditorLOK.COMPONENT_NAME;

/**
 * Property Editor for Resources on a Company
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/6/17
 */
@I18NFile(
    symbolPrefix = "net.venturetech.event_api.profile.ui.company.resource.CompanyResourcePropertyEditor",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Resource Editor"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.Resource.EDIT,
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Company.Resource.EDIT,
        properties = {
            @URLProperty(name = URLProperties.REPOSITORY_ITEM, type = ResourceRepositoryItem.class),
            @URLProperty(
                name = URLProperties.RESOURCE_TYPE,
                type = ResourceType.class,
                converter = ResourceTypeURLConfigPropertyConverter.class),
            @URLProperty(name = URLProperties.REPOSITORY, type = Repository.class),
            @URLProperty(name = URLProperties.REPOSITORY_OWNER, type = Profile.class)
        },
        pathInfoPattern = URLProperties.REPOSITORY_ITEM_PATH_INFO
                          + URLProperties.RESOURCE_TYPE_PATH_INFO
                          + URLProperties.REPOSITORY_PATH_INFO
                          + URLProperties.REPOSITORY_OWNER_PATH_INFO
    )
)
public class CompanyResourcePropertyEditor extends AbstractProfileResourcePropertyEditor
{
    @Autowired private CompanyResourcePermissionCheck _permissionCheck;

    /**
     * Instantiates a new Company resource property editor.
     */
    public CompanyResourcePropertyEditor()
    {
        super();
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
    }

    @Override
    protected String getManagementApplicationFunction()
    {
        return ApplicationFunctions.Company.Resource.MANAGEMENT;
    }

    @Override
    protected String getViewerApplicationFunction()
    {
        return ApplicationFunctions.Company.Resource.VIEW;
    }

    //Used by ApplicationFunction
    @SuppressWarnings("unused")
    void configure(ParsedRequest request)
    {
        ResourceRepositoryItem value = request.getPropertyValue(URLProperties.REPOSITORY_ITEM);
        ResourceType resourceType = request.getPropertyValue(URLProperties.RESOURCE_TYPE);
        Repository repo = request.getPropertyValue(URLProperties.REPOSITORY);
        Profile owner = request.getPropertyValue(URLProperties.REPOSITORY_OWNER);

        configure(value, resourceType, repo, owner, _permissionCheck);
    }
}
