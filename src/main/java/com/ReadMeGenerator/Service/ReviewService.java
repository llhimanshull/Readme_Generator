package com.ReadMeGenerator.Service;

import com.ReadMeGenerator.Model.Review;

import com.ReadMeGenerator.Repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;


    public String createReview(String name, String review) {
        Review review_new = new Review();
        try{
            review_new.setName(name);
            review_new.setReview(review);
            reviewRepository.save(review_new);
            return "redirect:/review";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void getAll(Model model) {
        try{
            model.addAttribute("review", reviewRepository.findAll());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
