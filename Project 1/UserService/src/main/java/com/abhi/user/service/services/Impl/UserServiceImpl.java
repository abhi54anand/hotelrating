package com.abhi.user.service.services.Impl;

import com.abhi.user.service.entities.Hotel;
import com.abhi.user.service.entities.Rating;
import com.abhi.user.service.entities.User;
import com.abhi.user.service.exceptions.ResourceNotFoundException;
import com.abhi.user.service.repositories.UserRepository;
import com.abhi.user.service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);



    @Override
    public User saveUser(User user) {
        String randomUserId = UUID.randomUUID().toString();
        user.setUserId(randomUserId);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(String UserId) {

        User user = userRepository.findById(UserId).orElseThrow( ()-> new ResourceNotFoundException("User with given id is not found on the server"+UserId));


        // http://localhost:8083/ratings/users/836fd44b-cc82-4bd4-acec-81914de687c8

        Rating[] ratingsOfUser = restTemplate.getForObject("http://localhost:8083/ratings/users/"+user.getUserId(), Rating[].class);


        logger.info("{} ",ratingsOfUser);

        List<Rating> ratings = Arrays.stream(ratingsOfUser).toList();

        List<Rating> ratingList = ratings.stream().map(rating -> {



        //http://localhost:8082/hotels/2dd61973-c458-40c2-b369-28bb892fd9c3


        ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://localhost:8082/hotels/"+rating.getHotelId(),Hotel.class);
        Hotel hotel = forEntity.getBody();
        logger.info("response status code: {}",forEntity.getStatusCode());

        rating.setHotel(hotel);

        return rating;

        }).collect(Collectors.toList());










        user.setRatings(ratingList);
        return user;

    }
}
