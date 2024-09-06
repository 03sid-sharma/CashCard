package example.cashcard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    
    @GetMapping("/{requestedId}")
    private ResponseEntity<String> findById(@PathVariable Long requestedId){
        ResponseEntity<String> response = ResponseEntity.ok("{}");
        return response;
    }
}
