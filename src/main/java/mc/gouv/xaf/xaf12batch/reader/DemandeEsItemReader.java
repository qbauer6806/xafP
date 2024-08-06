package mc.gouv.xaf.xaf12batch.reader;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.annotation.PostConstruct;
import mc.gouv.xaf.xaf12batch.dto.DemandeEsDTO;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DemandeEsItemReader implements ItemReader<DemandeEsDTO> {

    private final RestHighLevelClient client;
    private SearchResponse searchResponse;
    private int currentIndex = 0;

    @Autowired
    public DemandeEsItemReader(RestHighLevelClient client) {
        this.client = client;
    }

    @PostConstruct
    public void init() throws IOException {
        SearchRequest searchRequest = new SearchRequest("stage-index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery().mustNot(existsQuery("identifiantDemande")));
        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    }

    @Override
    public DemandeEsDTO read() throws Exception {
        if (currentIndex < searchResponse.getHits().getHits().length) {
            System.out.println("DemandeEsItemReader");
            System.out.println(searchResponse.getHits().getHits().length);
            SearchHit searchHit = searchResponse.getHits().getHits()[currentIndex++];
            System.out.println("searchHit.getSourceAsString()");
            System.out.println(searchHit.getSourceAsString());
            return new ObjectMapper().readValue(searchHit.getSourceAsString(), DemandeEsDTO.class);
        } else {
            return null;
        }
    }
}
