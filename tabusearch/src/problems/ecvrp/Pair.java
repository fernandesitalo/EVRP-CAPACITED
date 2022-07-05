package problems.ecvrp;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pair {
    public Double cost;
    public Movement mov;
}