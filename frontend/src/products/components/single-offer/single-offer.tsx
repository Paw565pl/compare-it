import { Button } from "@/core/components/ui/button";
import { ShopImage } from "@/products/components/index";
import { formatCurrency } from "@/products/utils/format-currency";
import Link from "next/link";

interface SingleOfferProps {
  readonly shop: string;
  readonly shopLogoUrl: string;
  readonly url: string;
  readonly priceHistory: PriceHistoryProps[];
}

interface PriceHistoryProps {
  readonly timestamp: string;
  readonly price: number;
  readonly currency: string;
  readonly promoCode: boolean;
  readonly isAvailable: boolean;
  readonly condition: string;
}

const SingleOffer = ({
  shop,
  shopLogoUrl,
  url,
  priceHistory,
}: SingleOfferProps) => {
  const formattedPrice = formatCurrency(
    priceHistory[priceHistory.length - 1].price,
    priceHistory[priceHistory.length - 1].currency,
  );

  return (
    <div className="flex items-center justify-between bg-white p-4">
      <Link href={url} target="_blank" rel="nofollow noopener">
        <ShopImage name={shop} imageUrl={shopLogoUrl} />
      </Link>
      <div className="flex items-center">
        <div className="mr-4 flex flex-col items-center">
          <Link
            href={url}
            target="_blank"
            rel="nofollow noopener"
            className="justify-center text-lg font-semibold text-secondary"
          >
            {formattedPrice}
          </Link>
          <div className="text-sm">
            {priceHistory[priceHistory.length - 1].isAvailable ? (
              <span className="font-semibold text-green-600">
                Produkt dostępny
              </span>
            ) : (
              <span className="font-semibold text-red-600">
                Produkt niedostępny
              </span>
            )}
          </div>
        </div>
        <Button
          asChild
          className="bg-secondary font-semibold shadow-none hover:bg-hover"
        >
          <Link href={url} target="_blank" rel="nofollow noopener">
            PRZEJDŹ DO OFERTY
          </Link>
        </Button>
      </div>
    </div>
  );
};

export { SingleOffer };
