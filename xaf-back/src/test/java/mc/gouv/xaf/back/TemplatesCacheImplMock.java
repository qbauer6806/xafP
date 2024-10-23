package mc.gouv.xaf.back;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.mail.MailTemplateMock;
import mc.gouv.xaf.back.service.templates.TemplatesCache;
import mc.gouv.xaf.shared.dto.TemplateDTO;

@Component
@Profile("test")
public class TemplatesCacheImplMock implements TemplatesCache {

    @Override
    public Map<Integer, TemplateDTO> getAll() {
        Map<Integer, TemplateDTO> map = new HashMap<Integer, TemplateDTO>();
        map.put(123, givenSubjectTemplateDTO("fr"));
        map.put(456, givenContentTemplateDTO("fr"));
        map.put(110, givenSubjectTemplateDTO("en"));
        map.put(220, givenContentTemplateDTO("en"));

        return map;
    }

    @Override
    public TemplateDTO get(Integer key) {
        return givenContentTemplateDTO("fr");
    }

    @Override
    public TemplateDTO get(Integer key, boolean forceUpdate) {
        return givenContentTemplateDTO("fr");
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void add(Integer key, TemplateDTO value) {
    }

    @Override
    public Collection<TemplateDTO> getValues() {
        List<TemplateDTO> dtos = new ArrayList<TemplateDTO>();
        dtos.add(givenContentTemplateDTO("fr"));
        dtos.add(givenContentTemplateDTO("en"));

        return dtos;
    }

    @Override
    public Collection<Integer> getKeys() {
        List<Integer> keys = new ArrayList<Integer>();
        keys.add(123);
        return keys;
    }

    @Override
    public TemplateDTO getTemplate(String codeTemplate, String langue) {

        if (codeTemplate.equals("123")) {
            return givenSubjectTemplateDTO(langue);
        }
        if (codeTemplate.equals("456")) {
            return givenContentTemplateDTO(langue);
        }

        return null;
    }

    private TemplateDTO givenContentTemplateDTO(String langue) {
        TemplateDTO dto = new TemplateDTO();
        dto.setCode("456");
        dto.setLangue(langue);
        dto.setPkTemplates(22);

        if ("en".equals(langue)) {
            dto.setContenu(MailTemplateMock.accepteContentEN);
        } else {
            dto.setContenu(MailTemplateMock.accepteContentFR);
        }

        return dto;
    }

    private TemplateDTO givenSubjectTemplateDTO(String langue) {
        TemplateDTO dto = new TemplateDTO();
        dto.setCode("123");
        dto.setLangue(langue);
        dto.setPkTemplates(11);

        if ("en".equals(langue)) {
            dto.setContenu(MailTemplateMock.acceptSubjectEN);
        } else {
            dto.setContenu(MailTemplateMock.acceptSubjectFR);
        }

        return dto;
    }
}
