import { Router } from 'express';
import { getIpLocation, geocodeAddress, getPlaceAutocomplete } from '../controllers/geocodingController';

const router = Router();

router.get('/ip-location', getIpLocation);
router.get('/geocode', geocodeAddress);
router.get('/autocomplete', getPlaceAutocomplete);

export default router;
