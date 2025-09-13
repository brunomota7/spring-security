package br.com.springsecurity.controller;

import br.com.springsecurity.dto.CreateTweetDto;
import br.com.springsecurity.dto.FeedDto;
import br.com.springsecurity.service.TweetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tweets")
public class TweetController {

    private final TweetService tweetService;

    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    @PostMapping
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDto dto,
                                            JwtAuthenticationToken token) {
        tweetService.createTweet(dto, token.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        FeedDto feed = tweetService.getFeed(page, pageSize);
        return ResponseEntity.ok(feed);
    }

    @DeleteMapping("/{tweet_id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable Long tweet_id,
                                            JwtAuthenticationToken token) {
        tweetService.deleteTweet(tweet_id, token.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
