# Time-Based Evaluation Strategy (Schedules)

Automatically enable or disable features based on time windows.

## 📝 Description
The **ScheduleFeatureStrategy** allows developers to automate the lifecycle of a feature using time-based triggers. This is ideal for scheduled product launches, marketing campaigns, or maintenance windows.

## ✅ When to Use
-   **Product Launches**: Automatically enable a feature at midnight for a worldwide release.
-   **Limited-Time Offers**: Enable a discount or seasonal banner for a specific week.
-   **Maintenance/Sunset**: Automatically disable legacy components after a cut-off date.

## ⚙️ Example Configuration
```json
{
  "strategy": "ScheduleFeatureStrategy",
  "cron": "0 0 1 1 * ?"  // Every Jan 1st at midnight
}
```

## ⚖️ Pros and Cons
| Pros | Cons |
| :--- | :--- |
| Set-and-forget automation. | Timezone alignment complexities. |
| Avoid manual launch errors. | Difficult to handle drift or rollback. |
| Perfect for marketing syncs. | Hard to manually override easily. |
