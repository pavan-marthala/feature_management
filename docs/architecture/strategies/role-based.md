# Role-Based Evaluation Strategy

Evaluate features based on user roles and permissions.

## 📝 Description
The **RoleBasedFeatureStrategy** enables features for specific segments of users defined by roles. This is ideal for internal-only releases, admin tools, or tiered product levels (e.g., Free vs. Premium).

## ✅ When to Use
-   **Admin Tools**: Show advanced configuration options for staff only.
-   **Internal Releases**: Test new features on employees before customers.
-   **Tiered Access**: Provide exclusive features for 'PREMIUM' or 'ENTERPRISE' users.

## ⚙️ Example Configuration
```json
{
  "strategy": "JWTClaimFeatureStrategy",
  "claims": [
    { "roles": ["ADMIN", "SUPPORT", "TESTER"] }
  ]
}
```

## ⚖️ Pros and Cons
| Pros | Cons |
| :--- | :--- |
| Scalable segmentation. | Relies on consistent role mapping. |
| Fits modern RBAC systems. | Potential drift between auth and evaluation. |
| Efficient for broad testing. | Harder to debug individual users. |
