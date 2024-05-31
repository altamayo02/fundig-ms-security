package edu.prog3.mssecurity.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.prog3.mssecurity.Models.Loyalty;
import edu.prog3.mssecurity.Models.User;
import edu.prog3.mssecurity.Repositories.LoyaltyRepository;

@CrossOrigin
@RestController
@RequestMapping("api/loyalties")
public class LoyaltiesController {
    @Autowired
    private LoyaltyRepository theLoyaltyRepository;

    @GetMapping("{id}")
    public Loyalty find(@PathVariable String id) {
        Loyalty theLoyalty = this.theLoyaltyRepository
                .find(id)
                .orElse(null);
        return theLoyalty;
    }

    @GetMapping("user/{id}")
    public Loyalty findByUser(@PathVariable String userId) {
        Loyalty theLoyalty = this.theLoyaltyRepository
                .getLoyaltyByUser(userId)
                .orElse(null);
        return theLoyalty;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Loyalty create(@RequestBody Loyalty theNewLoyalty) {
        theNewLoyalty.setPoints(20);
        return this.theLoyaltyRepository.save(theNewLoyalty);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("exchange")
    public Loyalty exchange(@RequestBody User theUser) {
        Loyalty theLoyalty = this.findByUser(theUser.get_id());
        try {
            theLoyalty.exchangePoints();
        } catch (Exception e) {
            return null;
        }
        return this.theLoyaltyRepository.save(theLoyalty);
    }
}
