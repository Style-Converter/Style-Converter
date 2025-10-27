import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Style Converter - Visual Test",
  description: "Visual testing environment for CSS component styles",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
