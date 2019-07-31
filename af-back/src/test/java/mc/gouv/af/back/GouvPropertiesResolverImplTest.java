package mc.gouv.af.back;

import org.springframework.stereotype.Component;

import mc.gouv.af.back.properties.GouvPropertiesResolver;

@Component

public class GouvPropertiesResolverImplTest implements GouvPropertiesResolver {

    @Override
    public String getFileUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFileJwt() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMailUrl() {
        // TODO Auto-generated method stub
        return "localhost:30485";
    }

    @Override
    public String getMailJwt() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProcessDefinitionKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDemarcheId() {
        return "TESTDEM";
    }

    @Override
    public String getUsagersRestUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPaysRestUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFrontUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBackUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContainerId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGouvSharedEnv() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGouvSharedEnvColor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContactSupportUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFrontSharedKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHelpUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getUsagersCacheDuration() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getFrontFormStartPage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSearchHighlightPreTags() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSearchHighlightPostTags() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getJmsPort() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsDataDir() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsRedeliveryDelay() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsRedeliveryMultiplier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsRedeliveryMaxAttemps() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsDlq() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsTopic() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsHost() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsSenderUser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsSenderPassword() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsConsumerUser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJmsConsumerPassword() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEsClusterName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEsHost() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getEsPort() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEsNodeName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSubscriptionKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getEsReindexBulkSize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getNovalidate() {
        // TODO Auto-generated method stub
        return false;
    }

	@Override
	public String getGouvSharedLogonUrl() {
		// TODO Auto-generated method stub
		return null;
	}

}
