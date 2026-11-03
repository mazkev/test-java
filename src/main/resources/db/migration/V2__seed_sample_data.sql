-- 1. Buat User Penjual (Toko Kopi Nusantara) dengan password terenkripsi: rahasiaKopi123
INSERT INTO users (id, name, email, password, role)
VALUES (1, 'Toko Kopi Nusantara', 'kopinusantara@mail.com', '$2a$10$mqS4UPEBAMgfs3iyySyqreCJRZNsD7t21r5P5b8n2Vdxujg607Lca', 'SELLER');

-- 2. Buat User Pembeli (Budi Santoso) dengan password: passwordBudi123
INSERT INTO users (id, name, email, password, role)
VALUES (2, 'Budi Santoso', 'budi@mail.com', '$2a$10$mqS4UPEBAMgfs3iyySyqreCJRZNsD7t21r5P5b8n2Vdxujg607Lca', 'BUYER');

-- 3. Buat Produk ID 1 yang otomatis terikat ke Toko Kopi Nusantara (seller_id = 1)
INSERT INTO products (id, name, description, price, stock, category, seller_id, version)
VALUES (1, 'Kopi Gayo Arabika Asli', 'Biji kopi sangrai premium 250g', 85000.0, 50, 'Minuman', 1, 0);
