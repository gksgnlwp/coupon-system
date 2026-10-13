package com.hwije.coupon_system.simulation;

import com.hwije.coupon_system.dto.SimulationResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/simulations")
@ConditionalOnProperty(name = "simulation.enabled", havingValue = "true")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/coupons/{couponId}/issue")
    public SimulationResponse simulateIssue(
            @PathVariable Long couponId,
            @RequestParam(defaultValue = "1000") int users,
            @RequestParam(defaultValue = "100") int threads
    ) {
        return simulationService.simulateIssue(couponId, users, threads);
    }
}
