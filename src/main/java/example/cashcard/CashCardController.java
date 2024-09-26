package example.cashcard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    
    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId){
        // ResponseEntity<String> response = ResponseEntity.ok("{}");
        if (requestedId.equals(99L)) {
            CashCard cashCard = new CashCard(99L, 325.50);
            return ResponseEntity.ok(cashCard);
        } else
            return ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<Void> createCashCard(){
        return null;
    }
}
