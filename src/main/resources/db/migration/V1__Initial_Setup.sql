CREATE TABLE short_url (
    slug VARCHAR(16) NOT NULL,
    id SERIAL,
    owner VARCHAR(16) NOT NULL,
    original_url VARCHAR(2048) NOT NULL,
    expires_at TIMESTAMPTZ NULL,
    PRIMARY KEY (slug, id),
    CONSTRAINT unique_slug UNIQUE(slug)
)
PARTITION BY HASH (slug);

CREATE TABLE short_url_p0 PARTITION OF short_url FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE short_url_p1 PARTITION OF short_url FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE short_url_p2 PARTITION OF short_url FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE short_url_p3 PARTITION OF short_url FOR VALUES WITH (MODULUS 4, REMAINDER 3);

CREATE INDEX idx_short_url_owner_p0 ON short_url_p0(slug);
CREATE INDEX idx_short_url_owner_p1 ON short_url_p1(slug);
CREATE INDEX idx_short_url_owner_p2 ON short_url_p2(slug);
CREATE INDEX idx_short_url_owner_p3 ON short_url_p3(slug);

CREATE INDEX idx_owner_original_url_p0 ON short_url_p0(owner, original_url);
CREATE INDEX idx_owner_original_url_p1 ON short_url_p1(owner, original_url);
CREATE INDEX idx_owner_original_url_p2 ON short_url_p2(owner, original_url);
CREATE INDEX idx_owner_original_url_p3 ON short_url_p3(owner, original_url);

CREATE INDEX idx_short_url_expires_at_p0 ON short_url_p0(expires_at);
CREATE INDEX idx_short_url_expires_at_p1 ON short_url_p1(expires_at);
CREATE INDEX idx_short_url_expires_at_p2 ON short_url_p2(expires_at);
CREATE INDEX idx_short_url_expires_at_p3 ON short_url_p3(expires_at);
