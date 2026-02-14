import dotenv from 'dotenv';

dotenv.config();

const IPINFO_TOKEN = process.env.IPINFO_TOKEN || '';
const GOOGLE_MAPS_API_KEY = (process.env.GOOGLE_MAPS_API_KEY || '').trim();

const normalizeClientIp = (ip?: string): string | null => {
    if (!ip) {
        return null;
    }

    // Take the first IP when multiple are forwarded and strip IPv4-mapped IPv6 prefix
    const firstIp = ip.split(',')[0]?.trim() ?? '';
    const cleanedIp = firstIp.replace('::ffff:', '');

    // Ignore loopback/private ranges to avoid always resolving to the server location
    const privateIpPatterns = [
        /^127\./,
        /^10\./,
        /^192\.168\./,
        /^172\.(1[6-9]|2[0-9]|3[0-1])\./,
        /^::1$/,
    ];

    if (!cleanedIp || privateIpPatterns.some((regex) => regex.test(cleanedIp))) {
        return null;
    }

    return cleanedIp;
};

export interface GeoLocation {
    lat: number;
    lng: number;
    city?: string;
    region?: string;
    country?: string;
}

export const getLocationFromIpInfo = async (clientIp?: string): Promise<GeoLocation | null> => {
    try {
        const normalizedIp = normalizeClientIp(clientIp);
        const baseUrl = normalizedIp
            ? `https://ipinfo.io/${normalizedIp}/json`
            : 'https://ipinfo.io/json';

        const url = IPINFO_TOKEN ? `${baseUrl}?token=${IPINFO_TOKEN}` : baseUrl;

        const response = await fetch(url);

        if (!response.ok) {
            console.error('IPInfo API error:', response.status);
            return null;
        }

        const data = await response.json() as {
            loc?: string;
            city?: string;
            region?: string;
            country?: string;
        };

        if (!data.loc) {
            return null;
        }

        const parts = data.loc.split(',');
        const lat = parseFloat(parts[0] ?? '0') || 0;
        const lng = parseFloat(parts[1] ?? '0') || 0;

        return {
            lat,
            lng,
            city: data.city,
            region: data.region,
            country: data.country,
        };
    } catch (error) {
        console.error('Error fetching IP location:', error);
        return null;
    }
};

export interface PlacePrediction {
    description: string;
    place_id: string;
}

export const getPlaceAutocomplete = async (input: string): Promise<PlacePrediction[]> => {
    try {
        if (!GOOGLE_MAPS_API_KEY) {
            console.error('Google Maps API key not configured');
            return [];
        }

        const encodedInput = encodeURIComponent(input);
        const url = `https://maps.googleapis.com/maps/api/place/autocomplete/json?input=${encodedInput}&types=(cities)&key=${GOOGLE_MAPS_API_KEY}`;

        const response = await fetch(url);

        if (!response.ok) {
            console.error('Google Places API error:', response.status);
            return [];
        }

        const data = await response.json() as {
            status: string;
            error_message?: string;
            predictions?: Array<{
                description: string;
                place_id: string;
            }>;
        };

        if (data.error_message) {
            console.error('Places API error message:', data.error_message);
        }

        if (data.status !== 'OK' || !data.predictions) {
            return [];
        }

        return data.predictions.map(p => ({
            description: p.description,
            place_id: p.place_id
        }));
    } catch (error) {
        console.error('Error fetching place autocomplete:', error);
        return [];
    }
};

export const geocodeAddress = async (address: string): Promise<GeoLocation | null> => {
    try {
        if (!GOOGLE_MAPS_API_KEY) {
            console.error('Google Maps API key not configured');
            return null;
        }

        const encodedAddress = encodeURIComponent(address);
        const url = `https://maps.googleapis.com/maps/api/geocode/json?address=${encodedAddress}&key=${GOOGLE_MAPS_API_KEY}`;

        const response = await fetch(url);

        if (!response.ok) {
            console.error('Google Geocoding API error:', response.status);
            return null;
        }

        const data = await response.json() as {
            status: string;
            error_message?: string;
            results?: Array<{
                geometry?: {
                    location?: {
                        lat: number;
                        lng: number;
                    };
                };
                formatted_address?: string;
            }>;
        };

        if (data.error_message) {
            console.error('Geocoding API error message:', data.error_message);
        }

        if (data.status !== 'OK' || !data.results || data.results.length === 0) {
            console.error('No geocoding results found for address:', address);
            return null;
        }

        const location = data.results[0]?.geometry?.location;
        if (!location) {
            return null;
        }

        return {
            lat: location.lat,
            lng: location.lng,
        };
    } catch (error) {
        console.error('Error geocoding address:', error);
        return null;
    }
};
