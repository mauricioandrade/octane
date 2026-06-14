package com.octane.serviceorder.repository;

import com.octane.serviceorder.domain.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrder, UUID>, JpaSpecificationExecutor<ServiceOrder> {
    List<ServiceOrder> findByPlateOrderByOpenedAtDesc(String plate);
}
