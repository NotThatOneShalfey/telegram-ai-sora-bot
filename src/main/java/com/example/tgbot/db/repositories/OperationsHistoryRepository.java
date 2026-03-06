package com.example.tgbot.db.repositories;

import com.example.tgbot.data.GenerationType;
import com.example.tgbot.data.HistoryOperationType;
import com.example.tgbot.db.OperationsHistory;
import com.example.tgbot.db.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface OperationsHistoryRepository extends JpaRepository<OperationsHistory, UUID> {

    List<OperationsHistory> findByUserIdOrderByOperationTimestampDesc(User user);

    List<OperationsHistory> findByUserIdAndGenerationTypeOrderByOperationTimestampDesc(User user, GenerationType generationType);

    @Query("SELECT COALESCE(SUM(ABS(oh.balanceChange)), 0) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users " +
            "AND oh.operationTimestamp >= :from AND oh.operationTimestamp <= :to")
    double sumAbsBalanceChangeForGeneration(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to);

    @Query("SELECT COALESCE(SUM(oh.costRub), 0) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users " +
            "AND oh.operationTimestamp >= :from AND oh.operationTimestamp <= :to")
    BigDecimal sumCostRubForGeneration(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to);

    @Query("SELECT COALESCE(SUM(ABS(oh.balanceChange)), 0) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users")
    double sumAbsBalanceChangeForGenerationAllTime(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users);

    @Query("SELECT COALESCE(SUM(oh.costRub), 0) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users")
    BigDecimal sumCostRubForGenerationAllTime(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users);

    @Query("SELECT COUNT(oh) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users " +
            "AND oh.operationTimestamp >= :from AND oh.operationTimestamp <= :to")
    long countGenerationsForUsers(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to);

    @Query("SELECT COUNT(oh) FROM OperationsHistory oh " +
            "WHERE oh.operationType = :opType AND oh.userId IN :users")
    long countGenerationsForUsersAllTime(
            @Param("opType") HistoryOperationType opType,
            @Param("users") List<User> users);
}
