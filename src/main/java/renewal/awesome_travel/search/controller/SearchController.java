package renewal.awesome_travel.search.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.search.CodeSearchService;
import renewal.awesome_travel.search.dto.CodeSearchResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final CodeSearchService codeSearchService;

    @GetMapping("/code/search")
    public List<CodeSearchResponse> searchCode(@RequestParam String keyword) {
        if (log.isDebugEnabled()) {
            log.debug("searchCode: keyword={}", keyword);
        }
        return codeSearchService.searchFromCache(keyword);
    }
}
