import { Request, Response } from 'express';
import * as geocodingService from '../services/geocodingService';

export const getIpLocation = async (req: Request, res: Response): Promise<void> => {
    try {
        const clientIp = req.ip || req.headers['x-forwarded-for'] as string;
        const location = await geocodingService.getLocationFromIpInfo(clientIp);

        if (!location) {
            res.status(404).json({ error: 'Could not determine location' });
            return;
        }

        res.json(location);
    } catch (error) {
        console.error('Error getting IP location:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

export const geocodeAddress = async (req: Request, res: Response): Promise<void> => {
    try {
        const { address } = req.query;

        if (!address || typeof address !== 'string') {
            res.status(400).json({ error: 'Address is required' });
            return;
        }

        const location = await geocodingService.geocodeAddress(address);

        if (!location) {
            res.status(404).json({ error: 'Could not geocode address' });
            return;
        }

        res.json(location);
    } catch (error) {
        console.error('Error geocoding address:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

export const getPlaceAutocomplete = async (req: Request, res: Response): Promise<void> => {
    try {
        const { input } = req.query;

        if (!input || typeof input !== 'string') {
            res.status(400).json({ error: 'Input is required' });
            return;
        }

        const predictions = await geocodingService.getPlaceAutocomplete(input);
        res.json({ predictions });
    } catch (error) {
        console.error('Error fetching autocomplete:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};
