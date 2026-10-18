-- Development-only seed data for the unit-of-measure service.
-- PostgreSQL dialect: runs after Hibernate creates/updates the schema (deferred
-- data-source initialization) and is idempotent, so a service restart never
-- duplicates rows. Status rows resolve by name, so a re-created status row can
-- never leave dangling uom rows.
INSERT INTO public.uom_status (name, description, is_usable)
VALUES ('Active', 'Unit of measure is currently active and can be used in transactions', true),
       ('Inactive', 'Unit of measure is inactive and cannot be used in new transactions', false),
       ('Deprecated', 'Unit of measure has been deprecated and should not be used', false)
ON CONFLICT (name) DO NOTHING;

-- The uom ids 1 and 2 are a deliberate id contract with the inventory seed
-- (Kilogram, Gram).
INSERT INTO public.uom (id, name, description, conversion_factor_to_base, uom_status_id)
VALUES (1, 'Kilogram', 'Base unit of mass in SI', 1.000,
        (SELECT id FROM public.uom_status WHERE name = 'Active')),
       (2, 'Gram', 'Derived mass unit equal to one thousandth of a kilogram', 0.001,
        (SELECT id FROM public.uom_status WHERE name = 'Active'))
ON CONFLICT DO NOTHING;

-- Realign the identity sequences so application inserts continue after the seeded ids.
SELECT setval(pg_get_serial_sequence('public.uom_status', 'id'),
              COALESCE(max(id), 1), max(id) IS NOT NULL)
FROM public.uom_status;
SELECT setval(pg_get_serial_sequence('public.uom', 'id'),
              COALESCE(max(id), 1), max(id) IS NOT NULL)
FROM public.uom;
