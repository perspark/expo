// Copyright 2015-present 650 Industries. All rights reserved.

#import "EXReactAppManager.h"

#import "RCTLog.h"

NS_ASSUME_NONNULL_BEGIN

@interface EXReactAppManager ()

@property (nonatomic, strong) NSString *versionSymbolPrefix;
@property (nonatomic, strong) NSString *validatedVersion;

// versioned
@property (nonatomic, strong) id versionManager;

- (BOOL)isReadyToLoad;

- (void)computeVersionSymbolPrefix;

- (NSString *)bundleNameForJSResource;
- (EXCachedResourceBehavior)cacheBehaviorForJSResource;

- (NSDictionary * _Nullable)launchOptionsForBridge;
- (NSDictionary * _Nullable)initialPropertiesForRootView;
- (NSString *)applicationKeyForRootView;

- (RCTLogFunction)logFunction;
- (RCTLogLevel)logLevel;

- (void)registerBridge;
- (void)unregisterBridge;

- (Class)versionedClassFromString: (NSString *)classString;

@end

NS_ASSUME_NONNULL_END
