package de.ibmix.magkit.test.cms.freemarker;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Channel;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.dam.templating.functions.DamTemplatingFunctions;
import info.magnolia.freemarker.FreemarkerConfig;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.module.site.ConfiguredSite;
import info.magnolia.module.site.DefaultSiteManager;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.module.site.SiteModule;
import info.magnolia.module.site.functions.SiteFunctions;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.AppendableOnlyOutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderExceptionHandler;
import info.magnolia.rendering.renderer.FreemarkerRenderer;
import info.magnolia.templating.functions.TemplatingFunctions;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAggregationState;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for Freemarker template script tests.
 *
 * @author lars.gendner
 */
//CHECKSTYLE:OFF
public abstract class FreemarkerTest {
    private ComponentProvider _componentProvider;
    private Site _site;
    private SiteModule _siteModule;
    private SiteManager _siteManager;
    private ServerConfiguration _serverConfiguration;
    private I18nContentSupport _i18nContentSupport;
    private LinkTransformerManager _linkTransformerManager;
    private DefaultMessagesManager _messagesManager;
    private FreemarkerHelper _freemarkerHelper;
    private ExtendedAggregationState _aggregationState;
    private RenderingContext _renderingCtx;
    private TemplatingFunctions _templatingFunctions;
    private SiteFunctions _siteFunctions;
    private DamTemplatingFunctions _damTemplatingFunctions;

    /**
     * Returns the locale under which the tests shall run.
     *
     * @return a locale
     */
    protected abstract Locale getLocale();

    /**
     * Returns the i18n basename under which the tests shall run.
     *
     * @return a i18n basename
     */
    protected abstract String getI18nBasename();

    /**
     * Set up the environment for a single test.
     * This can be overridden in sub-classes to extend and change behavior.
     * There is no need to tag the overridden method again with BeforeEach.
     *
     * @throws RepositoryException repository exception
     */
    public void setupEnvironment() throws RepositoryException {
        _componentProvider = ComponentsMockUtils.getComponentProvider();

        _site = new ConfiguredSite();
        _siteModule = new SiteModule(new Node2BeanProcessorImpl(null, null), null, null, null);
        _siteModule.setSite(_site);

        final Provider<SiteModule> stkModuleProvider = () -> _siteModule;
        _siteManager = new DefaultSiteManager(stkModuleProvider, _templatingFunctions);
        ComponentsMockUtils.getComponentProvider().setInstance(SiteManager.class, _siteManager);

        _serverConfiguration = new ServerConfiguration();

        _i18nContentSupport = new DefaultI18nContentSupport();
        ComponentsMockUtils.getComponentProvider().setInstance(I18nContentSupport.class, _i18nContentSupport);

        _linkTransformerManager = new LinkTransformerManager();

        _messagesManager = mock(DefaultMessagesManager.class);
        Messages messages = mock(Messages.class);
        when(_messagesManager.getMessagesInternal(anyString(), any())).thenReturn(messages);
        ComponentsMockUtils.getComponentProvider().setInstance(MessagesManager.class, _messagesManager);

        TranslationService translationService = mock(TranslationService.class);
        _freemarkerHelper = new FreemarkerHelper(new FreemarkerConfig(), translationService);

        _aggregationState = new ExtendedAggregationState();
        _aggregationState.setChannel(new Channel());
        _aggregationState.setLocale(getLocale());
        _aggregationState.setSite(_site);
        mockWebContext(stubAggregationState(_aggregationState));
        final RenderExceptionHandler exceptionHandler = new RenderExceptionHandler() {
            @Override
            public void handleException(RenderException renderException, RenderingContext renderingContext) {
                throw new RuntimeException(renderException);
            }

            @Override
            public void handleException(RenderException exception, Appendable out) {
                throw new RuntimeException(exception);
            }
        };

        _renderingCtx = new AggregationStateBasedRenderingContext(_aggregationState, exceptionHandler);

        final Provider<AggregationState> aggregationStateProvider = () -> _aggregationState;
        _templatingFunctions = new TemplatingFunctions(aggregationStateProvider);
        ComponentsMockUtils.getComponentProvider().setInstance(TemplatingFunctions.class, _templatingFunctions);
    }

    /**
     * Delivers the rendered output as string.
     *
     * @param node jcr node
     * @param freemarkerScriptName script name
     * @param additionalContextObjects additional context objects
     * @return rendering result
     * @throws RenderException render exception
     */
    public String getRenderingResult(Node node, final String freemarkerScriptName, Map<String, Object> additionalContextObjects) throws RenderException {
        final FreemarkerTestRenderableDefinition definition = new FreemarkerTestRenderableDefinition(freemarkerScriptName);
        final FreemarkerRenderer renderer = new FreemarkerRenderer(_freemarkerHelper, null);
        final StringBuilder output = new StringBuilder();
        final Map<String, Object> contextObjects = new HashMap<>();

        getRenderingCtx().push(node, definition, new AppendableOnlyOutputProvider(output));
        contextObjects.put("cmsfn", getTemplatingFunctions());
        contextObjects.put("sitefn", getSiteFunctions());
        contextObjects.put("damfn", getDamTemplatingFunctions());

        if (getI18nBasename() != null) {
            contextObjects.put("i18n", new MessagesWrapper(getI18nBasename(), getLocale()));
        }
        if (additionalContextObjects != null) {
            contextObjects.putAll(additionalContextObjects);
        }

        renderer.render(getRenderingCtx(), contextObjects);
        return output.toString();
    }

    public ComponentProvider getComponentProvider() {
        return _componentProvider;
    }

    public void setComponentProvider(ComponentProvider componentProvider) {
        _componentProvider = componentProvider;
    }

    public Site getSite() {
        return _site;
    }

    public void setSite(Site site) {
        _site = site;
    }

    public SiteModule getSiteModule() {
        return _siteModule;
    }

    public void setSiteModule(SiteModule siteModule) {
        _siteModule = siteModule;
    }

    public SiteManager getSiteManager() {
        return _siteManager;
    }

    public void setSiteManager(SiteManager siteManager) {
        _siteManager = siteManager;
    }

    public ServerConfiguration getServerConfiguration() {
        return _serverConfiguration;
    }

    public void setServerConfiguration(ServerConfiguration serverConfiguration) {
        _serverConfiguration = serverConfiguration;
    }

    public I18nContentSupport getI18nContentSupport() {
        return _i18nContentSupport;
    }

    public void setI18nContentSupport(I18nContentSupport i18nContentSupport) {
        _i18nContentSupport = i18nContentSupport;
    }

    public LinkTransformerManager getLinkTransformerManager() {
        return _linkTransformerManager;
    }

    public void setLinkTransformerManager(LinkTransformerManager linkTransformerManager) {
        _linkTransformerManager = linkTransformerManager;
    }

    public DefaultMessagesManager getMessagesManager() {
        return _messagesManager;
    }

    public void setMessagesManager(DefaultMessagesManager messagesManager) {
        _messagesManager = messagesManager;
    }

    public FreemarkerHelper getFreemarkerHelper() {
        return _freemarkerHelper;
    }

    public void setFreemarkerHelper(FreemarkerHelper freemarkerHelper) {
        _freemarkerHelper = freemarkerHelper;
    }

    public ExtendedAggregationState getAggregationState() {
        return _aggregationState;
    }

    public void setAggregationState(ExtendedAggregationState aggregationState) {
        _aggregationState = aggregationState;
    }

    public RenderingContext getRenderingCtx() {
        return _renderingCtx;
    }

    public void setRenderingCtx(RenderingContext renderingCtx) {
        _renderingCtx = renderingCtx;
    }

    public TemplatingFunctions getTemplatingFunctions() {
        return _templatingFunctions;
    }

    public void setTemplatingFunctions(TemplatingFunctions templatingFunctions) {
        _templatingFunctions = templatingFunctions;
    }

    public SiteFunctions getSiteFunctions() {
        return _siteFunctions;
    }

    public void setSiteFunctions(SiteFunctions siteFunctions) {
        _siteFunctions = siteFunctions;
    }

    public DamTemplatingFunctions getDamTemplatingFunctions() {
        return _damTemplatingFunctions;
    }

    public void setDamTemplatingFunctions(DamTemplatingFunctions damTemplatingFunctions) {
        _damTemplatingFunctions = damTemplatingFunctions;
    }

}
