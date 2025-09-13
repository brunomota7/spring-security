package br.com.springsecurity.service;

import br.com.springsecurity.dto.CreateTweetDto;
import br.com.springsecurity.dto.FeedDto;
import br.com.springsecurity.dto.FeedItemDto;
import br.com.springsecurity.entities.Role;
import br.com.springsecurity.entities.Tweet;
import br.com.springsecurity.repository.TweetRepository;
import br.com.springsecurity.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class TweetService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public TweetService(TweetRepository tweetRepository,
                        UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    public void createTweet(CreateTweetDto dto, String userId) {
        var user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        var tweet = new Tweet();
        tweet.setUser(user);
        tweet.setContent(dto.content());

        tweetRepository.save(tweet);
    }

    public FeedDto getFeed(int page, int pageSize) {
        var tweets = tweetRepository.findAll(
                PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(tweet -> new FeedItemDto(
                        tweet.getTweetId(),
                        tweet.getContent(),
                        tweet.getUser().getUsername()
                ));

        return new FeedDto(
                tweets.getContent(),
                page,
                pageSize,
                tweets.getTotalPages(),
                tweets.getTotalElements()
        );
    }

    public void deleteTweet(Long tweetId, String userId) {
        var user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        var tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet not found!"));

        var isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleId().equals(Role.Values.ADMIN.name()));

        if (isAdmin || tweet.getUser().getUserId().equals(UUID.fromString(userId))) tweetRepository.delete(tweet);
        else throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to delete this tweet");
    }

}
