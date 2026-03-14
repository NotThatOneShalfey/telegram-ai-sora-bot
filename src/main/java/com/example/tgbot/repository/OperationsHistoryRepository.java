package com.example.tgbot.repository;

import com.example.tgbot.domain.enums.GenerationStatus;
import com.example.tgbot.domain.enums.GenerationType;
import com.example.tgbot.domain.enums.HistoryOperationType;
import com.example.tgbot.domain.model.OperationsHistory;
import com.example.tgbot.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationsHistoryRepository extends JpaRepository<OperationsHistory, UUID> {

    List<OperationsHistory> findByUserIdOrderByOperationTimestampDesc(User user);

    List<OperationsHistory> findByUserIdAndGenerationTypeOrderByOperationTimestampDesc(User user, GenerationType generationType);

    @Query("SELECT oh FROM OperationsHistory oh WHERE oh.userId = :user AND oh.generationType = :type " +
            "AND (oh.status = 'SUCCESS' OR oh.status IS NULL) ORDER BY oh.operationTimestamp DESC")
    List<OperationsHistory> findSuccessfulByUserIdAndGenerationTypeOrderByOperationTimestampDesc(
            @Param("user") User user, @Param("type") GenerationType generationType);

    @Query("SELECT oh FROM OperationsHistory oh WHERE oh.userId = :user AND oh.generationType = :type " +
            "AND (oh.status = 'SUCCESS' OR oh.status = 'PROCESSING' OR oh.status IS NULL) ORDER BY oh.operationTimestamp DESC")
    List<OperationsHistory> findSuccessfulOrProcessingByUserIdAndGenerationTypeOrderByOperationTimestampDesc(
            @Param("user") User user, @Param("type") GenerationType generationType);

    Optional<OperationsHistory> findByTaskId(String taskId);

    @Query("SELECT COALESCE(SUM(ABS(oh.balanceChange)), 0) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users " +
            "AND (oh.status = 'SUCCESS' OR oh.status IS NULL) " +
            "AND oh.operationTimestamp >= :from AND oh.operationTimestamp <= :to")
    double sumAbsBalanceChangeForGeneration(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to);

    @Query("SELECT COALESCE(SUM(oh.costRub), 0) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users " +
            "AND (oh.status = 'SUCCESS' OR oh.status IS NULL) " +
            "AND oh.operationTimestamp >= :from AND oh.operationTimestamp <= :to")
    BigDecimal sumCostRubForGeneration(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to);

    @Query("SELECT COALESCE(SUM(ABS(oh.balanceChange)), 0) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users " +
            "AND (oh.status = 'SUCCESS' OR oh.status IS NULL)")
    double sumAbsBalanceChangeForGenerationAllTime(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users);

    @Query("SELECT COALESCE(SUM(oh.costRub), 0) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users " +
            "AND (oh.status = 'SUCCESS' OR oh.status IS NULL)")
    BigDecimal sumCostRubForGenerationAllTime(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users);

    @Query("SELECT COUNT(oh) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users " +
            "AND (oh.status = 'SUCCESS' OR oh.status IS NULL) " +
            "AND oh.operationTimestamp >= :from AND oh.operationTimestamp <= :to")
    long countGenerationsForUsers(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to);

    @Query("SELECT COUNT(oh) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users " +
            "AND (oh.status = 'SUCCESS' OR oh.status IS NULL)")
    long countGenerationsForUsersAllTime(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users);
}
