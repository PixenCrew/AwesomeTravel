package renewal.awesome_travel.search.controller;

import java.util.List;

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

    private final CodeSearchService codeSearchService;

    @GetMapping("/code/search")
    public List<CodeSearchResponse> searchCode(@RequestParam String keyword) {
        System.out.println("searchCode :" + keyword);
        return codeSearchService.searchFromCache(keyword);
    }
}
