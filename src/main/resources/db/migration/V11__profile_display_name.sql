-- Nombre visible que llega en account.registered; lo exige el contrato profile.updated.
-- Cifrado AES-256-GCM (base64), igual que phone_enc.
ALTER TABLE profiles ADD COLUMN display_name_enc TEXT;
