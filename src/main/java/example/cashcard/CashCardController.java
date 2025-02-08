package example.cashcard;
import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository){
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal){
        CashCard cashCard = findCashCard(requestedId, principal);
        if(cashCard != null)
            return ResponseEntity.ok(cashCard);
        else
            return ResponseEntity.notFound().build();
        // For Learning
        // ResponseEntity<String> response = ResponseEntity.ok("{}");
        // if (requestedId.equals(99L)) {
        //     CashCard cashCard = new CashCard(99L, 325.50);
        //     return ResponseEntity.ok(cashCard);
        // } else
        //     return ResponseEntity.notFound().build();
    }

    private CashCard findCashCard(Long requestedId, Principal principal) {
        CashCard cashCard =cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
        return cashCard;
    }

    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
        URI locationOfNewCashCard = ucb.path("cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal){
        CashCard cashCard = findCashCard(requestedId, principal);

        if (cashCard != null) {
            CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
            cashCardRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        } else
            return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId, Principal principal) {
        if(cashCardRepository.existsByIdAndOwner(requestedId, principal.getName())){
            cashCardRepository.deleteById(requestedId);
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.notFound().build();        
    }

    // pre-defined CRUD method for JSON array of objects
    // @GetMapping()
    // private ResponseEntity<Iterable<CashCard>> findAll(){
    //     return ResponseEntity.ok(cashCardRepository.findAll());
    // }

    @GetMapping()
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "amount"))));
        return ResponseEntity.ok(page.getContent());
    }
}
